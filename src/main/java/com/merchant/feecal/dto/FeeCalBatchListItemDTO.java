package com.merchant.feecal.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 批次列表单条记录
 */
@Data
public class FeeCalBatchListItemDTO {
    private String batchNo;
    private String batchStatus;
    private String billingDataStatus;
    private String merchantType;
    private String merchantCode;
    private BigDecimal depositAmountDeduct;
    private BigDecimal depositBalanceRemain;
    private BigDecimal unpaidAmountRemain;
    private LocalDateTime createdAt;
}
