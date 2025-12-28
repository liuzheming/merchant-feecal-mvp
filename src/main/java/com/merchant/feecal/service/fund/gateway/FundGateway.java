package com.merchant.feecal.service.fund.gateway;

/**
 * 对接资金系统的网关接口
 */
public interface FundGateway {

    /**
     * 执行资金指令
     */
    FundGatewayResponse execute(FundGatewayRequest request);
}
