package com.merchant.feecal.repo;

import com.merchant.dao.tables.pojos.FeeCalFundInstructionEntity;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.merchant.dao.Tables.FEE_CAL_FUND_INSTRUCTION;

/**
 * fee_cal_fund_instruction 表访问
 */
@Component
public class FundInstructionRepo {

    @Resource
    private DSLContext dslContext;

    public void insertIgnore(FeeCalFundInstructionEntity entity) {
        dslContext.insertInto(FEE_CAL_FUND_INSTRUCTION)
                .set(FEE_CAL_FUND_INSTRUCTION.BATCH_NO, entity.getBatchNo())
                .set(FEE_CAL_FUND_INSTRUCTION.TERM_INST_ID, entity.getTermInstId())
                .set(FEE_CAL_FUND_INSTRUCTION.SETTLE_SUBJECT_TYPE, entity.getSettleSubjectType())
                .set(FEE_CAL_FUND_INSTRUCTION.SETTLE_SUBJECT_NO, entity.getSettleSubjectNo())
                .set(FEE_CAL_FUND_INSTRUCTION.PAYER_TYPE, entity.getPayerType())
                .set(FEE_CAL_FUND_INSTRUCTION.PAYER_NO, entity.getPayerNo())
                .set(FEE_CAL_FUND_INSTRUCTION.PAYEE_TYPE, entity.getPayeeType())
                .set(FEE_CAL_FUND_INSTRUCTION.PAYEE_NO, entity.getPayeeNo())
                .set(FEE_CAL_FUND_INSTRUCTION.FUND_DIRECTION, entity.getFundDirection())
                .set(FEE_CAL_FUND_INSTRUCTION.FUND_BIZ_TYPE, entity.getFundBizType())
                .set(FEE_CAL_FUND_INSTRUCTION.ACCOUNT_TYPE, entity.getAccountType())
                .set(FEE_CAL_FUND_INSTRUCTION.SHOULD_AMOUNT, entity.getShouldAmount())
                .set(FEE_CAL_FUND_INSTRUCTION.ACTUAL_AMOUNT, entity.getActualAmount())
                .set(FEE_CAL_FUND_INSTRUCTION.FUND_STATUS, entity.getFundStatus())
                .set(FEE_CAL_FUND_INSTRUCTION.CALLBACK_STATUS, entity.getCallbackStatus())
                .set(FEE_CAL_FUND_INSTRUCTION.FUND_ORDER_ID, entity.getFundOrderId())
                .set(FEE_CAL_FUND_INSTRUCTION.FUND_CHANNEL, entity.getFundChannel())
                .set(FEE_CAL_FUND_INSTRUCTION.FUND_ORDER_INFO, toJson(entity.getFundOrderInfo()))
                .set(FEE_CAL_FUND_INSTRUCTION.ATTACHMENT_IDS, entity.getAttachmentIds())
                .onDuplicateKeyIgnore()
                .execute();
    }

    public List<FeeCalFundInstructionEntity> queryByBatchNo(String batchNo) {
        return dslContext.selectFrom(FEE_CAL_FUND_INSTRUCTION)
                .where(FEE_CAL_FUND_INSTRUCTION.BATCH_NO.eq(batchNo))
                .orderBy(FEE_CAL_FUND_INSTRUCTION.ID.asc())
                .fetchInto(FeeCalFundInstructionEntity.class);
    }

    public Optional<FeeCalFundInstructionEntity> findById(Long id) {
        FeeCalFundInstructionEntity entity = dslContext.selectFrom(FEE_CAL_FUND_INSTRUCTION)
                .where(FEE_CAL_FUND_INSTRUCTION.ID.eq(id))
                .fetchOneInto(FeeCalFundInstructionEntity.class);
        return Optional.ofNullable(entity);
    }

    public boolean existsByBatchNo(String batchNo) {
        return dslContext.fetchExists(
                dslContext.selectOne()
                        .from(FEE_CAL_FUND_INSTRUCTION)
                        .where(FEE_CAL_FUND_INSTRUCTION.BATCH_NO.eq(batchNo))
        );
    }

    public void updateExecutionResult(Long id,
                                      String fundStatus,
                                      BigDecimal actualAmount,
                                      String fundOrderId,
                                      String fundChannel,
                                      String fundOrderInfo) {
        dslContext.update(FEE_CAL_FUND_INSTRUCTION)
                .set(FEE_CAL_FUND_INSTRUCTION.FUND_STATUS, fundStatus)
                .set(FEE_CAL_FUND_INSTRUCTION.ACTUAL_AMOUNT, actualAmount)
                .set(FEE_CAL_FUND_INSTRUCTION.FUND_ORDER_ID, fundOrderId)
                .set(FEE_CAL_FUND_INSTRUCTION.FUND_CHANNEL, fundChannel)
                .set(FEE_CAL_FUND_INSTRUCTION.FUND_ORDER_INFO, toJson(fundOrderInfo))
                .where(FEE_CAL_FUND_INSTRUCTION.ID.eq(id))
                .execute();
    }

    public void updateFundStatus(Long id, String fundStatus) {
        dslContext.update(FEE_CAL_FUND_INSTRUCTION)
                .set(FEE_CAL_FUND_INSTRUCTION.FUND_STATUS, fundStatus)
                .where(FEE_CAL_FUND_INSTRUCTION.ID.eq(id))
                .execute();
    }

    public void updateCallbackStatus(Long id, String callbackStatus) {
        dslContext.update(FEE_CAL_FUND_INSTRUCTION)
                .set(FEE_CAL_FUND_INSTRUCTION.CALLBACK_STATUS, callbackStatus)
                .where(FEE_CAL_FUND_INSTRUCTION.ID.eq(id))
                .execute();
    }

    public void updateAttachmentIds(Long id, String attachmentIds) {
        dslContext.update(FEE_CAL_FUND_INSTRUCTION)
                .set(FEE_CAL_FUND_INSTRUCTION.ATTACHMENT_IDS, attachmentIds)
                .where(FEE_CAL_FUND_INSTRUCTION.ID.eq(id))
                .execute();
    }

    private JSON toJson(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof JSON) {
            return (JSON) value;
        }
        return JSON.valueOf(String.valueOf(value));
    }
}
