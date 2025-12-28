package com.merchant.feecal.service.billing;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 账单聚合服务接口
 */
public interface IBillingAggregationService {

    /**
     * 聚合整个批次的欠费金额，返回 termCode -> amount
     */
    Map<String, BigDecimal> aggregateByTerm(String batchNo);

    /**
     * 计算单个 termCode 的账单欠费总额
     */
    BigDecimal aggregateUnpaidAmount(String batchNo, String termCode);
}
