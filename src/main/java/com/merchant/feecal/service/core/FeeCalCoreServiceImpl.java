package com.merchant.feecal.service.core;

import com.merchant.common.exception.ServiceException;
import com.merchant.dao.tables.pojos.FeeCalBatchEntity;
import com.merchant.dao.tables.pojos.FeeCalBillingSnapshotEntity;
import com.merchant.dao.tables.pojos.FeeCalFundInstructionEntity;
import com.merchant.dao.tables.pojos.FeeCalMerchantEntity;
import com.merchant.dao.tables.pojos.FeeCalTermDefEntity;
import com.merchant.dao.tables.pojos.FeeCalTermInstAllocEntity;
import com.merchant.dao.tables.pojos.FeeCalTermInstEntity;
import com.merchant.feecal.adapter.billing.BillingAdapter;
import com.merchant.feecal.dto.FeeCalStartRequest;
import com.merchant.feecal.dto.FeeCalStartResponse;
import com.merchant.feecal.repo.FeeCalBillingSnapshotRepo;
import com.merchant.feecal.repo.FeeCalBatchRepo;
import com.merchant.feecal.repo.FeeCalIdempotentRepo;
import com.merchant.feecal.repo.FeeCalMerchantRepo;
import com.merchant.feecal.repo.FeeCalTermInstAllocRepo;
import com.merchant.feecal.repo.FeeCalTermInstRepo;
import com.merchant.feecal.service.FeeCalConstants.Status.Batch;
import com.merchant.feecal.service.FeeCalConstants.Status.BillingData;
import com.merchant.feecal.service.FeeCalConstants.Status.Term;
import com.merchant.feecal.service.alloc.FeeCalTermInstAllocService;
import com.merchant.feecal.service.billing.IBillingAggregationService;
import com.merchant.feecal.service.billing.IFeeCalBillingManageService;
import com.merchant.feecal.service.fund.model.FundExecutionContext;
import com.merchant.feecal.termdef.TermDefService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.exception.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 费用清算核心服务实现
 */
@Slf4j
@Service
public class FeeCalCoreServiceImpl implements IFeeCalCoreService {

    @Resource
    private FeeCalBatchRepo feeCalBatchRepo;

    @Resource
    private FeeCalMerchantRepo feeCalMerchantRepo;
    @Resource
    private FeeCalIdempotentRepo feeCalIdempotentRepo;

    @Resource
    private FeeCalTermInstRepo feeCalTermInstRepo;

    @Resource
    private FeeCalTermInstAllocRepo termInstAllocRepo;

    @Resource
    private TermDefService termDefService;

    @Resource
    private IFeeCalBillingManageService feeCalBillingManageService;

    @Resource
    private IBillingAggregationService billingAggregationService;

    @Resource
    private BillingAdapter billingAdapter;

    @Resource
    private FeeCalBillingSnapshotRepo billingSnapshotRepo;

    @Resource
    private FeeCalTermInstAllocService termInstAllocService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FeeCalStartResponse start(FeeCalStartRequest request) {
        if (request == null) {
            throw new ServiceException("请求不能为空");
        }
        String requestId = request.getRequestId();
        FeeCalIdempotentEntity idempotent = null;
        if (StringUtils.isNotBlank(requestId)) {
            idempotent = feeCalIdempotentRepo.queryByBizAndRequestId(IdempotentBiz.FEE_CAL_START, requestId);
            if (idempotent != null && StringUtils.isNotBlank(idempotent.getBatchNo())) {
                FeeCalStartResponse response = new FeeCalStartResponse();
                response.setBatchNo(idempotent.getBatchNo());
                return response;
            }
        }
        String batchNo = generateBatchNo();

        if (StringUtils.isNotBlank(requestId)) {
            FeeCalIdempotentEntity newEntity = new FeeCalIdempotentEntity();
            newEntity.setBizType(IdempotentBiz.FEE_CAL_START);
            newEntity.setRequestId(requestId);
            newEntity.setStatus(IdempotentStatus.PROCESSING);
            newEntity.setBatchNo(batchNo);
            try {
                feeCalIdempotentRepo.insert(newEntity);
                idempotent = newEntity;
            } catch (DataAccessException ex) {
                FeeCalIdempotentEntity existing = feeCalIdempotentRepo.queryByBizAndRequestId(
                        IdempotentBiz.FEE_CAL_START, requestId);
                if (existing != null && StringUtils.isNotBlank(existing.getBatchNo())) {
                    FeeCalStartResponse response = new FeeCalStartResponse();
                    response.setBatchNo(existing.getBatchNo());
                    return response;
                }
                throw ex;
            }
        }

        FeeCalBatchEntity batchEntity = new FeeCalBatchEntity();
        batchEntity.setBatchNo(batchNo);
        batchEntity.setRequestId(requestId);
        batchEntity.setStatus(Batch.EDITING);
        batchEntity.setBillingDataStatus(BillingData.PENDING);
        batchEntity.setDepositBalanceTotal(BigDecimal.ZERO);
        batchEntity.setDepositAmountDeduct(BigDecimal.ZERO);
        batchEntity.setDepositBalanceRemain(BigDecimal.ZERO);
        batchEntity.setUnpaidAmountRemain(BigDecimal.ZERO);
        feeCalBatchRepo.insert(batchEntity);

        FeeCalMerchantEntity merchantEntity = new FeeCalMerchantEntity();
        merchantEntity.setBatchNo(batchNo);
        merchantEntity.setMerchantType(request.getMerchantType());
        merchantEntity.setMerchantCode(request.getMerchantCode());
        feeCalMerchantRepo.insert(merchantEntity);

        List<FeeCalTermDefEntity> termDefs = termDefService.loadEnabledTerms();
        if (CollectionUtils.isNotEmpty(termDefs)) {
            List<FeeCalTermInstEntity> termInsts = new ArrayList<>();
            for (FeeCalTermDefEntity termDef : termDefs) {
                FeeCalTermInstEntity termInst = new FeeCalTermInstEntity();
                termInst.setBatchNo(batchNo);
                termInst.setMerchantId(merchantEntity.getId());
                termInst.setTermCode(termDef.getCode());
                termInst.setStatus(Term.PENDING);
                termInst.setUnpaidAmount(BigDecimal.ZERO);
                termInst.setDeductFlag((byte) 0);
                termInst.setDeductAmount(BigDecimal.ZERO);
                termInst.setAutoLoad(termDef.getAutoLoad());
                termInsts.add(termInst);
            }
            feeCalTermInstRepo.batchInsert(termInsts);
        }

        FeeCalCoreContext coreContext = new FeeCalCoreContext(batchEntity, merchantEntity);

        try {
            feeCalBillingManageService.refreshBillingData(coreContext);
            aggregateAndApplyAutoLoadTerms(coreContext);
            termInstAllocService.rebuildAllocations(coreContext);
            if (idempotent != null) {
                feeCalIdempotentRepo.updateStatusAndBatchNo(
                        idempotent.getId(),
                        IdempotentStatus.SUCCESS,
                        batchNo,
                        null);
            }
        } catch (ServiceException ex) {
            if (idempotent != null) {
                feeCalIdempotentRepo.updateStatus(
                        idempotent.getId(),
                        IdempotentStatus.FAIL,
                        ex.getMessage());
            }
            LOGGER.warn("auto refresh billing data failed, batchNo={}", batchNo, ex);
        }

        FeeCalStartResponse response = new FeeCalStartResponse();
        response.setBatchNo(batchNo);
        return response;
    }

    private static final class IdempotentBiz {
        private static final String FEE_CAL_START = "FEE_CAL_START";
    }

    private static final class IdempotentStatus {
        private static final String PROCESSING = "PROCESSING";
        private static final String SUCCESS = "SUCCESS";
        private static final String FAIL = "FAIL";
    }

    /**
     * 将账单聚合结果回填到 autoLoad 的 TermInst
     */
    private void aggregateAndApplyAutoLoadTerms(FeeCalCoreContext coreContext) {
        if (coreContext == null || coreContext.getBatch() == null || coreContext.getMerchant() == null) {
            return;
        }
        String batchNo = coreContext.getBatch().getBatchNo();
        Long merchantId = coreContext.getMerchant().getId();
        Map<String, BigDecimal> aggregateMap = billingAggregationService.aggregateByTerm(batchNo);
        if (aggregateMap.isEmpty()) {
            return;
        }
        List<FeeCalTermInstEntity> termInsts = feeCalTermInstRepo.queryByBatchNoAndMerchantId(batchNo, merchantId);
        if (termInsts == null) {
            return;
        }
        for (FeeCalTermInstEntity termInst : termInsts) {
            if (termInst.getAutoLoad() == null || termInst.getAutoLoad() == 0) {
                continue;
            }
            BigDecimal amount = aggregateMap.getOrDefault(termInst.getTermCode(), BigDecimal.ZERO);
            termInst.setUnpaidAmount(amount);
            feeCalTermInstRepo.update(termInst);
        }
    }

    @Override
    public FeeCalCoreContext loadCoreContext(String batchNo) {
        FeeCalBatchEntity batch = feeCalBatchRepo.queryByBatchNo(batchNo);
        if (batch == null) {
            throw new ServiceException("批次不存在: " + batchNo);
        }
        List<FeeCalMerchantEntity> merchants = feeCalMerchantRepo.queryByBatchNo(batchNo);
        if (CollectionUtils.isEmpty(merchants)) {
            throw new ServiceException("批次对应的主体不存在: " + batchNo);
        }
        return new FeeCalCoreContext(batch, merchants.get(0));
    }

    @Override
    public Map<String, FeeCalTermDefEntity> loadEnabledTermDefinitions() {
        return termDefService.loadEnabledTerms()
                .stream()
                .collect(Collectors.toMap(FeeCalTermDefEntity::getCode, def -> def));
    }

    @Override
    public List<FeeCalTermInstEntity> queryTermInsts(String batchNo, Long merchantId) {
        return feeCalTermInstRepo.queryByBatchNoAndMerchantId(batchNo, merchantId);
    }

    @Override
    public void updateBatch(FeeCalBatchEntity batch) {
        feeCalBatchRepo.update(batch);
    }

    @Override
    public void batchUpdateTermStatus(String batchNo, String status) {
        feeCalTermInstRepo.batchUpdateStatus(batchNo, status);
    }

    @Override
    public void updateTermInst(FeeCalTermInstEntity termInst) {
        feeCalTermInstRepo.update(termInst);
    }


	@Override
	public void onFundInstructionSuccess(FeeCalFundInstructionEntity instruction, FundExecutionContext context) {
		if (instruction == null) {
			throw new ServiceException("资金指令缺失");
		}
		if (instruction.getTermInstId() == null) {
			LOGGER.info("instruction {} not bound to termInst, skip billing callback", instruction.getId());
			return;
		}
		FeeCalTermInstEntity termInst = feeCalTermInstRepo.findById(instruction.getTermInstId());
		if (termInst == null) {
			throw new ServiceException("费用项不存在，termInstId=" + instruction.getTermInstId());
		}
		List<FeeCalTermInstAllocEntity> allocEntities = termInstAllocRepo.listByTermInstId(termInst.getId());
		List<FeeCalTermInstAllocEntity> effectiveAllocs = allocEntities.stream()
				.filter(alloc -> alloc != null && alloc.getAllocFlag() != null && alloc.getAllocFlag() == 1)
				.collect(Collectors.toList());
		if (CollectionUtils.isEmpty(effectiveAllocs)) {
			LOGGER.info("termInst {} has no billing allocations, skip billing callback", termInst.getId());
			return;
		}
		List<Long> snapshotIds = effectiveAllocs.stream()
				.map(FeeCalTermInstAllocEntity::getBillingSnapshotId)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		Map<Long, FeeCalBillingSnapshotEntity> snapshotMap = billingSnapshotRepo.listByIds(snapshotIds)
				.stream()
				.collect(Collectors.toMap(FeeCalBillingSnapshotEntity::getId, entity -> entity, (a, b) -> a));
		BillingAdapter.BillingCallbackContext callbackContext = buildBillingCallbackContext(
				termInst, instruction, context, effectiveAllocs, snapshotMap);
		billingAdapter.pushFundResult(callbackContext);
	}

	private BillingAdapter.BillingCallbackContext buildBillingCallbackContext(FeeCalTermInstEntity termInst,
																			 FeeCalFundInstructionEntity instruction,
																			 FundExecutionContext context,
																			 List<FeeCalTermInstAllocEntity> allocs,
																			 Map<Long, FeeCalBillingSnapshotEntity> snapshotMap) {
		BillingAdapter.BillingCallbackContext callbackContext = new BillingAdapter.BillingCallbackContext();
		callbackContext.setBatchNo(termInst.getBatchNo());
		callbackContext.setTermCode(termInst.getTermCode());
		callbackContext.setTermInstId(termInst.getId());
		callbackContext.setFundInstructionId(instruction.getId());
		callbackContext.setFundBizType(instruction.getFundBizType());
		if (context != null) {
			callbackContext.setOperator(context.getOperator());
			callbackContext.setOperatorName(context.getOperatorName());
			callbackContext.setRemark(context.getRemark());
		}
		callbackContext.setInstructionShouldAmount(safeAmount(instruction.getShouldAmount()));
		callbackContext.setInstructionActualAmount(safeAmount(defaultIfNull(instruction.getActualAmount(), instruction.getShouldAmount())));
		List<BillingAdapter.BillingCallbackItem> items = new ArrayList<>();
		for (FeeCalTermInstAllocEntity alloc : allocs) {
			BillingAdapter.BillingCallbackItem item = new BillingAdapter.BillingCallbackItem();
			item.setBillingSnapshotId(alloc.getBillingSnapshotId());
			FeeCalBillingSnapshotEntity snapshot = snapshotMap.get(alloc.getBillingSnapshotId());
			item.setBillingOrderId(snapshot == null ? null : snapshot.getBillingKey());
			item.setAllocAmount(safeAmount(alloc.getAllocAmount()));
			items.add(item);
		}
		callbackContext.setItems(items);
		return callbackContext;
	}

	private BigDecimal defaultIfNull(BigDecimal primary, BigDecimal fallback) {
		return primary == null ? fallback : primary;
	}

	private BigDecimal safeAmount(BigDecimal amount) {
		return amount == null ? BigDecimal.ZERO : amount;
	}

	public void ensureBillingDataReady(FeeCalCoreContext coreContext) {
		if (coreContext == null) {
			throw new ServiceException("账单上下文缺失");
		}
		FeeCalBatchEntity batch = coreContext.getBatch();
		if (batch == null) {
			throw new ServiceException("批次不存在");
		}
		String billingStatus = batch.getBillingDataStatus();
		if (BillingData.READY.equals(billingStatus)) {
			return;
		}
		if (BillingData.LOADING.equals(billingStatus)) {
			throw new ServiceException("账单数据正在拉取中，请稍后再试");
		}
		if (BillingData.PENDING.equals(billingStatus)) {
			throw new ServiceException("账单数据尚未准备，请稍后再试");
		}
		if (BillingData.FAILED.equals(billingStatus)) {
			throw new ServiceException("账单拉取失败，请稍后再试");
		}
		throw new ServiceException("未知的账单状态: " + billingStatus);
	}

    private String generateBatchNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new Date());
        long timestamp = System.currentTimeMillis();
        return "FC" + dateStr + String.format("%06d", timestamp % 1000000);
    }
}
