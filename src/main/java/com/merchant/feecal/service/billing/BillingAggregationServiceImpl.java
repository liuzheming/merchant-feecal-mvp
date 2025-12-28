package com.merchant.feecal.service.billing;

import com.merchant.dao.tables.pojos.FeeCalBillingSnapshotEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 账单聚合服务实现：只负责根据快照计算金额
 */
@Service
public class BillingAggregationServiceImpl implements IBillingAggregationService {

    @Resource
    private IBillingSnapshotService billingSnapshotService;

    @Override
    public Map<String, BigDecimal> aggregateByTerm(String batchNo) {
        Map<String, BigDecimal> aggregateMap = billingSnapshotService.aggregateUnpaidAmount(batchNo);
        return aggregateMap == null ? Collections.emptyMap() : aggregateMap;
    }

    @Override
    public BigDecimal aggregateUnpaidAmount(String batchNo, String termCode) {
        List<FeeCalBillingSnapshotEntity> billingViews = billingSnapshotService.listBillingSnapshot(batchNo, termCode);
        if (billingViews == null || billingViews.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return billingViews.stream()
                .map(view -> view.getBillingUnpaidAmount() == null ? BigDecimal.ZERO : view.getBillingUnpaidAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
