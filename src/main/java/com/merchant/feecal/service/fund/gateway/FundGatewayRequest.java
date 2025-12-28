package com.merchant.feecal.service.fund.gateway;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 资金网关请求
 */
@Data
@Builder
public class FundGatewayRequest {
    private Long instructionId;
    private String batchNo;
    private String fundBizType;
    private String fundDirection;
    private String accountType;
    private BigDecimal shouldAmount;
    private String payerType;
    private String payerNo;
    private String payeeType;
    private String payeeNo;
    private String operator;
    private String operatorName;
    private String remark;
}
