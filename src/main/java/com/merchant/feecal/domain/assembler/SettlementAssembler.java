package com.merchant.feecal.domain.assembler;

import com.merchant.dao.tables.pojos.FeeCalBatchEntity;
import com.merchant.dao.tables.pojos.FeeCalMerchantEntity;
import com.merchant.dao.tables.pojos.FeeCalTermDefEntity;
import com.merchant.dao.tables.pojos.FeeCalTermInstEntity;
import com.merchant.feecal.domain.model.SettlementBatch;
import com.merchant.feecal.domain.model.SettlementSummary;
import com.merchant.feecal.domain.model.TermItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 领域对象装配器
 */
public final class SettlementAssembler {

    private SettlementAssembler() {
    }

    public static SettlementBatch toSettlementBatch(FeeCalBatchEntity batchEntity, FeeCalMerchantEntity merchantEntity) {
        SettlementBatch settlementBatch = new SettlementBatch();
        if (batchEntity != null) {
            settlementBatch.setBatchNo(batchEntity.getBatchNo());
            settlementBatch.setStatus(batchEntity.getStatus());
            settlementBatch.setBillingDataStatus(batchEntity.getBillingDataStatus());
            settlementBatch.setSummary(toSettlementSummary(batchEntity));
        }
        if (merchantEntity != null) {
            settlementBatch.setMerchantType(merchantEntity.getMerchantType());
            settlementBatch.setMerchantCode(merchantEntity.getMerchantCode());
        }
        return settlementBatch;
    }

    public static SettlementSummary toSettlementSummary(FeeCalBatchEntity batchEntity) {
        SettlementSummary summary = new SettlementSummary();
        if (batchEntity == null) {
            return summary;
        }
        summary.setDepositBalanceTotal(defaultBigDecimal(batchEntity.getDepositBalanceTotal()));
        summary.setDepositBalanceRemain(defaultBigDecimal(batchEntity.getDepositBalanceRemain()));
        summary.setDepositAmountDeduct(defaultBigDecimal(batchEntity.getDepositAmountDeduct()));
        summary.setUnpaidAmountRemain(defaultBigDecimal(batchEntity.getUnpaidAmountRemain()));
        return summary;
    }

    public static List<TermItem> toTermItems(List<FeeCalTermInstEntity> termInsts,
                                             Map<String, FeeCalTermDefEntity> termDefMap) {
        List<TermItem> result = new ArrayList<>();
        if (termInsts == null) {
            return result;
        }
        for (FeeCalTermInstEntity termInst : termInsts) {
            TermItem termItem = new TermItem();
            termItem.setCode(termInst.getTermCode());
            FeeCalTermDefEntity termDef = termDefMap.get(termInst.getTermCode());
            termItem.setName(termDef != null ? termDef.getName() : termInst.getTermCode());
            termItem.setStatus(termInst.getStatus());
            boolean autoLoad = termInst.getAutoLoad() != null && termInst.getAutoLoad() == 1;
            termItem.setAutoLoad(autoLoad);
            termItem.setUnpaidAmount(defaultBigDecimal(termInst.getUnpaidAmount()));
            termItem.setDeductFlag(termInst.getDeductFlag() != null && termInst.getDeductFlag() == 1);
            termItem.setDeductAmount(defaultBigDecimal(termInst.getDeductAmount()));
            result.add(termItem);
        }
        return result;
    }

    private static BigDecimal defaultBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
