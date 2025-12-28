package com.merchant.feecal.domain.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 费用项实例
 */
@Data
public class TermItem {
    private String code;
    private String name;
    private String status;
    private Boolean autoLoad;
    private BigDecimal unpaidAmount;
    private Boolean deductFlag;
    private BigDecimal deductAmount;
}
