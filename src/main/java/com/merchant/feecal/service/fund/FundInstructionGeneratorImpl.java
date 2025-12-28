package com.merchant.feecal.service.fund;

import com.merchant.dao.tables.pojos.FeeCalBatchEntity;
import com.merchant.dao.tables.pojos.FeeCalFundInstructionEntity;
import com.merchant.dao.tables.pojos.FeeCalMerchantEntity;
import com.merchant.dao.tables.pojos.FeeCalTermInstEntity;
import com.merchant.feecal.repo.FundInstructionRepo;
import com.merchant.feecal.service.FeeCalConstants.Fund;
import com.merchant.feecal.service.FeeCalConstants.Status.FundCallback;
import com.merchant.feecal.service.FeeCalConstants.Status.FundInstruction;
import com.merchant.feecal.service.core.FeeCalCoreContext;
import com.merchant.feecal.service.core.IFeeCalCoreService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * 资金指令生成
 */
@Service
public class FundInstructionGeneratorImpl implements IFundInstructionGenerator {

    @Resource
    private IFeeCalCoreService feeCalCoreService;

    @Resource
    private FundInstructionRepo fundInstructionRepo;

    @Override
    public void generateForBatch(String batchNo) {
        if (fundInstructionRepo.existsByBatchNo(batchNo)) {
            return;
        }
        FeeCalCoreContext context = feeCalCoreService.loadCoreContext(batchNo);
        FeeCalMerchantEntity merchant = context.getMerchant();
        FeeCalBatchEntity batch = context.getBatch();
        if (merchant == null || batch == null) {
            return;
        }
        List<FeeCalTermInstEntity> termInsts = feeCalCoreService.queryTermInsts(batchNo, merchant.getId());
        if (CollectionUtils.isNotEmpty(termInsts)) {
            for (FeeCalTermInstEntity termInst : termInsts) {
                if (shouldCreateDeductInstruction(termInst)) {
                    FeeCalFundInstructionEntity entity = buildDeductInstruction(batch, merchant, termInst);
                    fundInstructionRepo.insertIgnore(entity);
                }
            }
        }
        if (batch.getDepositBalanceRemain() != null
                && batch.getDepositBalanceRemain().compareTo(BigDecimal.ZERO) > 0) {
            FeeCalFundInstructionEntity refund = buildRefundInstruction(batch, merchant);
            fundInstructionRepo.insertIgnore(refund);
        }
    }

    private boolean shouldCreateDeductInstruction(FeeCalTermInstEntity termInst) {
        return termInst != null
                && termInst.getDeductAmount() != null
                && termInst.getDeductAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    private FeeCalFundInstructionEntity buildDeductInstruction(FeeCalBatchEntity batch,
                                                         FeeCalMerchantEntity merchant,
                                                         FeeCalTermInstEntity termInst) {
        FeeCalFundInstructionEntity entity = new FeeCalFundInstructionEntity();
        entity.setBatchNo(batch.getBatchNo());
        entity.setTermInstId(termInst.getId());
        entity.setSettleSubjectType(defaultValue(merchant.getMerchantType(), Fund.PartyType.MERCHANT));
        entity.setSettleSubjectNo(merchant.getMerchantCode());
        entity.setPayerType(Fund.PartyType.MERCHANT);
        entity.setPayerNo(merchant.getMerchantCode());
        entity.setPayeeType(Fund.PartyType.PLATFORM);
        entity.setPayeeNo(Fund.PartyType.PLATFORM);
        entity.setFundDirection(Fund.Direction.DEBIT);
        entity.setFundBizType(Fund.BizType.DEPOSIT_DEDUCT);
        entity.setAccountType(Fund.Account.DEPOSIT);
        entity.setShouldAmount(termInst.getDeductAmount());
        entity.setFundStatus(FundInstruction.PENDING);
        entity.setCallbackStatus(FundCallback.NOT_STARTED);
        return entity;
    }

    private FeeCalFundInstructionEntity buildRefundInstruction(FeeCalBatchEntity batch,
                                                         FeeCalMerchantEntity merchant) {
        FeeCalFundInstructionEntity entity = new FeeCalFundInstructionEntity();
        entity.setBatchNo(batch.getBatchNo());
        entity.setTermInstId(null);
        entity.setSettleSubjectType(defaultValue(merchant.getMerchantType(), Fund.PartyType.MERCHANT));
        entity.setSettleSubjectNo(merchant.getMerchantCode());
        entity.setPayerType(Fund.PartyType.PLATFORM);
        entity.setPayerNo(Fund.PartyType.PLATFORM);
        entity.setPayeeType(Fund.PartyType.MERCHANT);
        entity.setPayeeNo(merchant.getMerchantCode());
        entity.setFundDirection(Fund.Direction.CREDIT);
        entity.setFundBizType(Fund.BizType.DEPOSIT_REFUND);
        entity.setAccountType(Fund.Account.DEPOSIT);
        entity.setShouldAmount(batch.getDepositBalanceRemain());
        entity.setFundStatus(FundInstruction.PENDING);
        entity.setCallbackStatus(FundCallback.NOT_STARTED);
        return entity;
    }

    private String defaultValue(String value, String fallback) {
        return value == null ? fallback : value;
    }
}
