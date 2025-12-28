package com.merchant.feecal.service.billing;

import com.merchant.dao.tables.pojos.FeeCalBillingSnapshotEntity;
import com.merchant.feecal.repo.FeeCalBillingSnapshotRepo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 默认账单快照服务实现（只封装 Repo 操作）
 */
@Service
public class BillingSnapshotServiceImpl implements IBillingSnapshotService {

    @Resource
    private FeeCalBillingSnapshotRepo billingSnapshotRepo;

    @Override
    public List<FeeCalBillingSnapshotEntity> listBillingSnapshot(String batchNo, String termCode) {
        return billingSnapshotRepo.queryByBatchNoAndTermCode(batchNo, termCode);
    }

    @Override
    public void replaceBatch(String batchNo, List<FeeCalBillingSnapshotEntity> entities) {
        billingSnapshotRepo.replaceBatch(batchNo, entities);
    }

    @Override
    public void deleteByBatchNo(String batchNo) {
        billingSnapshotRepo.deleteByBatchNo(batchNo);
    }

    @Override
    public Map<String, BigDecimal> aggregateUnpaidAmount(String batchNo) {
        return billingSnapshotRepo.aggregateUnpaidAmount(batchNo);
    }
}
