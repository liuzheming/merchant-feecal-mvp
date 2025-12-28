package com.merchant.feecal.service;

import java.math.BigDecimal;

/**
 * 费用项输入接口（用于计算逻辑）
 */
public interface TermCardInput {
    String getCode();
    BigDecimal getUnpaidAmount();
    Boolean getDeductFlag();
    BigDecimal getDeductAmount();
}

