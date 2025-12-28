package com.merchant.feecal.service.fund.model;

import lombok.Builder;
import lombok.Data;

/**
 * 资金指令执行上下文
 */
@Data
@Builder
public class FundExecutionContext {
    private Long instructionId;
    private String operator;
    private String operatorName;
    private String remark;
}
