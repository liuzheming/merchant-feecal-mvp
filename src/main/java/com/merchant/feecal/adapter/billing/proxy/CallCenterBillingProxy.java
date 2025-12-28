package com.merchant.feecal.adapter.billing.proxy;

import com.merchant.feecal.adapter.billing.BillingAdapter;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 400 话务费上游 proxy（示例数据）
 */
@Component
public class CallCenterBillingProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(CallCenterBillingProxy.class);

    public CallCenterBillingResponse queryUnpaidOrders(String merchantCode) {
        CallCenterBillingResponse response = new CallCenterBillingResponse();
        response.setUcId("1000000031334387");
        response.setOrgCodes(Collections.singletonList("HHHT_14_282711"));
        response.setOwedAmount(new BigDecimal("33.04"));
        response.setUnPayedOrderInfoList(Arrays.asList(
                buildBill(merchantCode, 2453629L, new BigDecimal("29.94"), "202507"),
                buildBill(merchantCode, 2510330L, new BigDecimal("3.10"), "202508")
        ));
        return response;
    }

    public void callbackFundResult(BillingAdapter.BillingCallbackContext context) {
        LOGGER.info("mock call center billing callback, instructionId={}, orderCount={}",
                context.getFundInstructionId(),
                context.getItems() == null ? 0 : context.getItems().size());
    }

    private CallCenterBill buildBill(String merchantCode, Long orderId, BigDecimal amount, String cycle) {
        CallCenterBill bill = new CallCenterBill();
        bill.setOrderId(orderId);
        bill.setOrderNo("400" + orderId);
        bill.setMdmCode(merchantCode);
        bill.setShouldPayAmount(amount);
        bill.setRealPayAmount(BigDecimal.ZERO);
        bill.setCityCode("150100");
        bill.setCityName("呼和浩特");
        bill.setBillCycle(cycle);
        bill.setCalCycle(cycle);
        bill.setOwedAmount(amount);
        return bill;
    }

    @Data
    public static class CallCenterBillingResponse {
        private String ucId;
        private List<String> orgCodes;
        private BigDecimal owedAmount;
        private List<CallCenterBill> unPayedOrderInfoList;
    }

    @Data
    public static class CallCenterBill {
        private Long orderId;
        private String orderNo;
        private String mdmCode;
        private BigDecimal shouldPayAmount;
        private BigDecimal realPayAmount;
        private String cityCode;
        private String cityName;
        private String billCycle;
        private String calCycle;
        private BigDecimal owedAmount;
    }
}
