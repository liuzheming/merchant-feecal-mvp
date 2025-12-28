package com.merchant.feecal.adapter.billing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.merchant.dao.tables.pojos.FeeCalBillingSnapshotEntity;
import com.merchant.feecal.adapter.billing.proxy.CallCenterBillingProxy;
import com.merchant.feecal.adapter.billing.proxy.ElectronicSealBillingProxy;
import com.merchant.feecal.adapter.billing.proxy.MinimumGuaranteeBillingProxy;
import com.merchant.feecal.adapter.billing.proxy.CallCenterBillingProxy.CallCenterBill;
import com.merchant.feecal.adapter.billing.proxy.CallCenterBillingProxy.CallCenterBillingResponse;
import com.merchant.feecal.adapter.billing.proxy.ElectronicSealBillingProxy.ElectronicSealBillingResponse;
import com.merchant.feecal.adapter.billing.proxy.ElectronicSealBillingProxy.UnPayedOrderInfo;
import com.merchant.feecal.adapter.billing.proxy.MinimumGuaranteeBillingProxy.MinimumGuaranteeBillingResponse;
import com.merchant.feecal.adapter.billing.proxy.MinimumGuaranteeBillingProxy.MinimumGuaranteeOrder;
import com.merchant.feecal.service.FeeCalConstants.TermCode;
import org.apache.commons.collections4.CollectionUtils;
import org.jooq.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 默认 BillingAdapter 实现：调用多个 proxy 汇总账单快照
 */
@Component
public class DefaultBillingAdapter implements BillingAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultBillingAdapter.class);

    @Resource
    private ElectronicSealBillingProxy electronicSealBillingProxy;

    @Resource
    private MinimumGuaranteeBillingProxy minimumGuaranteeBillingProxy;

    @Resource
    private CallCenterBillingProxy callCenterBillingProxy;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public List<FeeCalBillingSnapshotEntity> pullBillingViews(BillingPullContext context) {
        List<FeeCalBillingSnapshotEntity> result = new ArrayList<>();
        result.addAll(convertElectronicSeal(context));
        result.addAll(convertMinimumGuarantee(context));
        result.addAll(convertCallCenter400(context));
        return result;
    }

    @Override
    public void pushFundResult(BillingCallbackContext context) {
        if (context == null || CollectionUtils.isEmpty(context.getItems())) {
            return;
        }
        if (TermCode.ELECTRONIC_SEAL.equals(context.getTermCode())) {
            electronicSealBillingProxy.callbackFundResult(context);
            return;
        }
        if (TermCode.MINIMUM_GUARANTEE.equals(context.getTermCode())) {
            minimumGuaranteeBillingProxy.callbackFundResult(context);
            return;
        }
        if (TermCode.CALL_400.equals(context.getTermCode())) {
            callCenterBillingProxy.callbackFundResult(context);
            return;
        }
        LOGGER.warn("unsupported termCode for billing callback, termCode={}, instructionId={}",
                context.getTermCode(), context.getFundInstructionId());
    }

    private List<FeeCalBillingSnapshotEntity> convertElectronicSeal(BillingPullContext context) {
        ElectronicSealBillingResponse response = electronicSealBillingProxy.queryUnpaidOrders(context.getMerchantCode());
        if (response == null || CollectionUtils.isEmpty(response.getUnPayedOrderInfoList())) {
            return Collections.emptyList();
        }
        List<FeeCalBillingSnapshotEntity> entities = new ArrayList<>();
        for (UnPayedOrderInfo order : response.getUnPayedOrderInfoList()) {
            entities.add(buildEntity(
                    context,
                    TermCode.ELECTRONIC_SEAL,
                    defaultString(order.getOrderNo(), order.getOrderId()),
                    order.getShouldPayAmount(),
                    order.getRealPayAmount(),
                    order.getShouldPayAmount().subtract(order.getRealPayAmount()),
                    order));
        }
        return entities;
    }

    private List<FeeCalBillingSnapshotEntity> convertMinimumGuarantee(BillingPullContext context) {
        MinimumGuaranteeBillingResponse response = minimumGuaranteeBillingProxy.queryUnpaidOrders(context.getMerchantCode());
        if (response == null || CollectionUtils.isEmpty(response.getUnPayedOrderInfoList())) {
            return Collections.emptyList();
        }
        List<FeeCalBillingSnapshotEntity> entities = new ArrayList<>();
        for (MinimumGuaranteeOrder order : response.getUnPayedOrderInfoList()) {
            entities.add(buildEntity(
                    context,
                    TermCode.MINIMUM_GUARANTEE,
                    defaultString(order.getOrderId(), order.getDepositNo()),
                    order.getShouldPayAmount(),
                    order.getRealPayAmount(),
                    order.getShouldPayAmount().subtract(order.getRealPayAmount()),
                    order));
        }
        return entities;
    }

    private List<FeeCalBillingSnapshotEntity> convertCallCenter400(BillingPullContext context) {
        CallCenterBillingResponse response = callCenterBillingProxy.queryUnpaidOrders(context.getMerchantCode());
        if (response == null || CollectionUtils.isEmpty(response.getUnPayedOrderInfoList())) {
            return Collections.emptyList();
        }
        List<FeeCalBillingSnapshotEntity> entities = new ArrayList<>();
        for (CallCenterBill bill : response.getUnPayedOrderInfoList()) {
            entities.add(buildEntity(
                    context,
                    TermCode.CALL_400,
                    defaultString(bill.getOrderId(), bill.getOrderNo()),
                    bill.getShouldPayAmount(),
                    bill.getRealPayAmount(),
                    bill.getShouldPayAmount().subtract(bill.getRealPayAmount()),
                    bill));
        }
        return entities;
    }

    private FeeCalBillingSnapshotEntity buildEntity(BillingPullContext context,
                                                String termCode,
                                                String billingKey,
                                                BigDecimal shouldPay,
                                                BigDecimal actualPay,
                                                BigDecimal unpaid,
                                                Object rawSource) {
        FeeCalBillingSnapshotEntity entity = new FeeCalBillingSnapshotEntity();
        entity.setBatchNo(context.getBatchNo());
        entity.setTermCode(termCode);
        entity.setBillingKey(billingKey);
        entity.setBillingShouldPayAmount(defaultAmount(shouldPay));
        entity.setBillingActualPayAmount(defaultAmount(actualPay));
        entity.setBillingUnpaidAmount(defaultAmount(unpaid));
        entity.setBillingSourceInfo(JSON.valueOf(toJson(rawSource)));
        return entity;
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String defaultString(Object primary, Object fallback) {
        if (primary != null && primary.toString().length() > 0) {
            return primary.toString();
        }
        return fallback == null ? "" : fallback.toString();
    }

    private String toJson(Object source) {
        try {
            return objectMapper.writeValueAsString(source);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("serialize billing source failed", e);
        }
    }
}
