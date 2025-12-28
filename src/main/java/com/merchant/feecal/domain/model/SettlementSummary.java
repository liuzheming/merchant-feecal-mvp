package com.merchant.feecal.domain.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 汇总结果快照
 */
@Data
public class SettlementSummary {
    private BigDecimal depositBalanceTotal = BigDecimal.ZERO;
    private BigDecimal depositBalanceRemain = BigDecimal.ZERO;
    private BigDecimal depositAmountDeduct = BigDecimal.ZERO;
    private BigDecimal unpaidAmountRemain = BigDecimal.ZERO;
}
