package com.merchant.feecal.domain.model;

import lombok.Data;

/**
 * 清算批次聚合根
 */
@Data
public class SettlementBatch {
    private String batchNo;
    private String status;
    private String billingDataStatus;
    private String merchantType;
    private String merchantCode;
    private SettlementSummary summary;
}
