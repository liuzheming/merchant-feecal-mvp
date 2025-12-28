package com.merchant.feecal.adapter.billing.proxy;

import com.merchant.feecal.adapter.billing.BillingAdapter;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * 电子章费上游 proxy（示例数据）
 */
@Component
public class ElectronicSealBillingProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElectronicSealBillingProxy.class);

    public ElectronicSealBillingResponse queryUnpaidOrders(String merchantCode) {
        ElectronicSealBillingResponse response = new ElectronicSealBillingResponse();
        response.setUcId("1000000026650067");
        response.setMdmCodes(Collections.singletonList(merchantCode));
        response.setOwedAmount(new BigDecimal("200.00"));
        response.setUnPayedOrderInfoList(Collections.singletonList(buildOrder(merchantCode)));
        return response;
    }

    public void callbackFundResult(BillingAdapter.BillingCallbackContext context) {
        LOGGER.info("mock electronic seal billing callback, instructionId={}, orderCount={}",
                context.getFundInstructionId(),
                context.getItems() == null ? 0 : context.getItems().size());
    }

    private UnPayedOrderInfo buildOrder(String merchantCode) {
        UnPayedOrderInfo info = new UnPayedOrderInfo();
        info.setOrderId(1672046L);
        info.setOrderNo("11102202511220900043");
        info.setAccountId(1028252L);
        info.setMdmCode(merchantCode);
        info.setShouldPayAmount(new BigDecimal("200.00"));
        info.setRealPayAmount(BigDecimal.ZERO);
        info.setCityCode("all");
        info.setOwedAmount(new BigDecimal("200.00"));
        return info;
    }

    @Data
    public static class ElectronicSealBillingResponse {
        private String ucId;
        private List<String> mdmCodes;
        private BigDecimal owedAmount;
        private List<UnPayedOrderInfo> unPayedOrderInfoList;
    }

    @Data
    public static class UnPayedOrderInfo {
        private Long orderId;
        private String orderNo;
        private Long accountId;
        private String mdmCode;
        private BigDecimal shouldPayAmount;
        private BigDecimal realPayAmount;
        private String cityCode;
        private BigDecimal owedAmount;
    }
}
