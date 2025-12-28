package com.merchant.feecal.service.alloc;

import com.merchant.common.exception.ServiceException;
import com.merchant.dao.tables.pojos.FeeCalBatchEntity;
import com.merchant.dao.tables.pojos.FeeCalBillingSnapshotEntity;
import com.merchant.dao.tables.pojos.FeeCalTermInstAllocEntity;
import com.merchant.dao.tables.pojos.FeeCalTermInstEntity;
import com.merchant.feecal.dto.FeeCalBillItemDTO;
import com.merchant.feecal.repo.FeeCalBillingSnapshotRepo;
import com.merchant.feecal.repo.FeeCalTermInstAllocRepo;
import com.merchant.feecal.repo.FeeCalTermInstRepo;
import com.merchant.feecal.service.core.FeeCalCoreContext;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 费用项账单分配服务
 */
@Service
public class FeeCalTermInstAllocService {

    @Resource
    private FeeCalTermInstAllocRepo termInstAllocRepo;
    @Resource
    private FeeCalBillingSnapshotRepo billingSnapshotRepo;
    @Resource
    private FeeCalTermInstRepo termInstRepo;
    /**
     * 重建指定批次的账单分配（start / refresh 时调用）
     */
    public void rebuildAllocations(FeeCalCoreContext context) {
        List<FeeCalTermInstEntity> termInsts = termInstRepo.queryByBatchNoAndMerchantId(
                context.getBatch().getBatchNo(),
                context.getMerchant().getId());
        if (!hasBillAllocTerms(termInsts)) {
            return;
        }
        rebuildAllocations(context.getBatch(), termInsts);
    }

    public void rebuildAllocations(FeeCalBatchEntity batch, List<FeeCalTermInstEntity> termInsts) {
        if (!hasBillAllocTerms(termInsts) || batch == null || CollectionUtils.isEmpty(termInsts)) {
            return;
        }
        Map<String, FeeCalTermInstEntity> termInstMap = termInsts.stream()
                .collect(Collectors.toMap(FeeCalTermInstEntity::getTermCode, inst -> inst, (a, b) -> a));
        List<Long> supportedTermInstIds = termInsts.stream()
                .filter(this::isBillAllocEnabled)
                .map(FeeCalTermInstEntity::getId)
                .collect(Collectors.toList());
        if (!supportedTermInstIds.isEmpty()) {
            termInstAllocRepo.deleteByTermInstIds(supportedTermInstIds);
        }
        for (Map.Entry<String, FeeCalTermInstEntity> entry : termInstMap.entrySet()) {
            if (!isBillAllocEnabled(entry.getValue())) {
                continue;
            }
            rebuildForTerm(batch, entry.getValue());
        }
    }

    public Map<Long, List<FeeCalBillItemDTO>> loadBillItems(String batchNo, List<FeeCalTermInstEntity> termInsts) {
        if (CollectionUtils.isEmpty(termInsts)) {
            return Collections.emptyMap();
        }
        List<Long> termInstIds = termInsts.stream()
                .filter(this::isBillAllocEnabled)
                .map(FeeCalTermInstEntity::getId)
                .collect(Collectors.toList());
        if (termInstIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<FeeCalTermInstAllocEntity> entities = termInstAllocRepo.listByTermInstIds(termInstIds);
        Map<Long, List<FeeCalTermInstAllocEntity>> allocMap = entities.stream()
                .collect(Collectors.groupingBy(FeeCalTermInstAllocEntity::getTermInstId));
        Map<Long, List<FeeCalBillItemDTO>> result = new HashMap<>();
        for (Long termInstId : termInstIds) {
            List<FeeCalTermInstAllocEntity> allocs = allocMap.get(termInstId);
            if (CollectionUtils.isEmpty(allocs)) {
                result.put(termInstId, Collections.emptyList());
            } else {
                result.put(termInstId, allocs.stream().map(this::toDTO).collect(Collectors.toList()));
            }
        }
        return result;
    }

    public void applyAllocations(FeeCalTermInstEntity termInst,
                                 List<FeeCalBillItemDTO> billItems,
                                 BigDecimal termDeductAmount) {
        if (!isBillAllocEnabled(termInst)) {
            return;
        }
        if (billItems == null) {
            return;
        }
        List<FeeCalTermInstAllocEntity> existing = termInstAllocRepo.listByTermInstId(termInst.getId());
        if (CollectionUtils.isEmpty(existing)) {
            throw new ServiceException("账单分配不存在，termCode=" + termInst.getTermCode());
        }
        Map<Long, FeeCalTermInstAllocEntity> allocMap = existing.stream()
                .collect(Collectors.toMap(FeeCalTermInstAllocEntity::getId, alloc -> alloc));
        BigDecimal sum = BigDecimal.ZERO;
        for (FeeCalBillItemDTO item : billItems) {
            FeeCalTermInstAllocEntity alloc = allocMap.get(item.getAllocId());
            if (alloc == null) {
                throw new ServiceException("账单分配不存在，allocId=" + item.getAllocId());
            }
            boolean flag = Boolean.TRUE.equals(item.getDeductFlag());
            BigDecimal amount = defaultAmount(item.getDeductAmount());
            if (!flag) {
                amount = BigDecimal.ZERO;
            }
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new ServiceException("抵扣金额不能小于0，账单：" + alloc.getBillingSnapshotId());
            }
            if (amount.compareTo(alloc.getUnpaidAmount()) > 0) {
                throw new ServiceException("抵扣金额不能超过账单未付金额，账单：" + alloc.getBillingSnapshotId());
            }
            termInstAllocRepo.updateAllocation(alloc.getId(), flag, amount);
            sum = sum.add(amount);
        }
        BigDecimal target = defaultAmount(termDeductAmount);
        if (sum.setScale(2, RoundingMode.HALF_UP).compareTo(target.setScale(2, RoundingMode.HALF_UP)) != 0) {
            throw new ServiceException("账单抵扣合计需等于费用项抵扣金额，termCode=" + termInst.getTermCode());
        }
    }

    public boolean hasBillAllocTerms(List<FeeCalTermInstEntity> termInsts) {
        if (CollectionUtils.isEmpty(termInsts)) {
            return false;
        }
        return termInsts.stream().anyMatch(this::isBillAllocEnabled);
    }

    public boolean isBillAllocEnabled(FeeCalTermInstEntity termInst) {
        return termInst != null && termInst.getBillingAllocEnabled() != null && termInst.getBillingAllocEnabled() == 1;
    }

    private void rebuildForTerm(FeeCalBatchEntity batch, FeeCalTermInstEntity termInst) {
        List<FeeCalBillingSnapshotEntity> snapshots = billingSnapshotRepo
                .queryByBatchNoAndTermCode(batch.getBatchNo(), termInst.getTermCode());
        if (CollectionUtils.isEmpty(snapshots)) {
            return;
        }
        List<FeeCalTermInstAllocEntity> entities = snapshots.stream()
                .map(snapshot -> toAllocEntity(batch.getBatchNo(), termInst, snapshot))
                .collect(Collectors.toList());
        termInstAllocRepo.batchInsert(entities);
    }

    private FeeCalTermInstAllocEntity toAllocEntity(String batchNo,
                                             FeeCalTermInstEntity termInst,
                                             FeeCalBillingSnapshotEntity snapshot) {
        FeeCalTermInstAllocEntity entity = new FeeCalTermInstAllocEntity();
        entity.setBatchNo(batchNo);
        entity.setTermInstId(termInst.getId());
        entity.setTermCode(termInst.getTermCode());
        entity.setBillingSnapshotId(snapshot.getId());
        entity.setUnpaidAmount(defaultAmount(snapshot.getBillingUnpaidAmount()));
        entity.setAllocFlag(0);
        entity.setAllocAmount(BigDecimal.ZERO);
        return entity;
    }

    private FeeCalBillItemDTO toDTO(FeeCalTermInstAllocEntity entity) {
        FeeCalBillItemDTO dto = new FeeCalBillItemDTO();
        dto.setAllocId(entity.getId());
        dto.setBillingSnapshotId(entity.getBillingSnapshotId());
        dto.setUnpaidAmount(entity.getUnpaidAmount());
        dto.setDeductFlag(entity.getAllocFlag() != null && entity.getAllocFlag() == 1);
        dto.setDeductAmount(entity.getAllocAmount());
        return dto;
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

}
