package com.merchant.feecal.service.fund.gateway;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 资金网关响应
 */
@Data
@Builder
public class FundGatewayResponse {
    private boolean success;
    private String fundOrderId;
    private String fundChannel;
    private BigDecimal actualAmount;
    private String rawResponse;
}
