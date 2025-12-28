package com.merchant.feecal.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 账单分配条目 DTO
 */
@Data
public class FeeCalBillItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long allocId;
    private Long billingSnapshotId;
    private String billingKey;
    private String billingDesc;
    private BigDecimal unpaidAmount;
    private Boolean deductFlag;
    private BigDecimal deductAmount;
}
