package com.merchant.feecal.service.fund.gateway;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * MockFundProxy：当前仍为 Mock，实现可替换为真实资金服务
 */
@Component
public class MockFundProxy implements FundGateway {

    private static final String MOCK_CHANNEL = "MOCK_CHANNEL";

    @Override
    public FundGatewayResponse execute(FundGatewayRequest request) {
        BigDecimal actualAmount = request.getShouldAmount() == null
                ? BigDecimal.ZERO
                : request.getShouldAmount();
        return FundGatewayResponse.builder()
                .success(true)
                .fundOrderId(buildOrderId(request))
                .fundChannel(MOCK_CHANNEL)
                .actualAmount(actualAmount)
                .rawResponse("{\"status\":\"SUCCESS\"}")
                .build();
    }

    private String buildOrderId(FundGatewayRequest request) {
        return "MOCK-" + request.getInstructionId() + "-" + Instant.now().toEpochMilli();
    }
}
