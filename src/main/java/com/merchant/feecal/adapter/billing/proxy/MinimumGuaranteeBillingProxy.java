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
 * 保底费上游 proxy（示例数据）
 */
@Component
public class MinimumGuaranteeBillingProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinimumGuaranteeBillingProxy.class);

    public MinimumGuaranteeBillingResponse queryUnpaidOrders(String merchantCode) {
        MinimumGuaranteeBillingResponse response = new MinimumGuaranteeBillingResponse();
        response.setUcId("1000000023103691");
        response.setMdmCodes(Collections.singletonList(merchantCode));
        response.setOwedAmount(new BigDecimal("801.68"));
        response.setUnPayedOrderInfoList(Collections.singletonList(buildOrder(merchantCode)));
        return response;
    }

    public void callbackFundResult(BillingAdapter.BillingCallbackContext context) {
        LOGGER.info("mock minimum guarantee billing callback, instructionId={}, orderCount={}",
                context.getFundInstructionId(),
                context.getItems() == null ? 0 : context.getItems().size());
    }

    private MinimumGuaranteeOrder buildOrder(String merchantCode) {
        MinimumGuaranteeOrder order = new MinimumGuaranteeOrder();
        order.setOrderId(625797L);
        order.setMdmCode(merchantCode);
        order.setShouldPayAmount(new BigDecimal("801.68"));
        order.setRealPayAmount(BigDecimal.ZERO);
        order.setOrgCode("QZ_14_1366445");
        order.setStoreId("436859");
        order.setDepositNo("BDF202503QZ_14_1366445");
        order.setCalCycleTime("按季");
        order.setBillCycle("202503");
        order.setOwedAmount(new BigDecimal("801.68"));
        return order;
    }

    @Data
    public static class MinimumGuaranteeBillingResponse {
        private String ucId;
        private List<String> mdmCodes;
        private BigDecimal owedAmount;
        private List<MinimumGuaranteeOrder> unPayedOrderInfoList;
    }

    @Data
    public static class MinimumGuaranteeOrder {
        private Long orderId;
        private String mdmCode;
        private BigDecimal shouldPayAmount;
        private BigDecimal realPayAmount;
        private String orgCode;
        private String storeId;
        private String depositNo;
        private String calCycleTime;
        private String billCycle;
        private BigDecimal owedAmount;
    }
}
