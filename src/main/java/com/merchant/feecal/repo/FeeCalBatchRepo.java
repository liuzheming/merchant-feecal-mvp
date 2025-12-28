package com.merchant.feecal.repo;

import com.merchant.dao.tables.pojos.FeeCalBatchEntity;
import com.merchant.dao.tables.records.FeeCalBatchRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.merchant.dao.Tables.FEE_CAL_BATCH;

/**
 * 费用清算批次表 Repository
 */
@Component
public class FeeCalBatchRepo {

    @Resource
    private DSLContext dslContext;

    /**
     * 根据批次号查询
     */
    public FeeCalBatchEntity queryByBatchNo(String batchNo) {
        return dslContext.selectFrom(FEE_CAL_BATCH)
                .where(FEE_CAL_BATCH.BATCH_NO.eq(batchNo))
                .fetchOneInto(FeeCalBatchEntity.class);
    }

    /**
     * 插入
     */
    public void insert(FeeCalBatchEntity entity) {
        FeeCalBatchRecord record = dslContext.newRecord(FEE_CAL_BATCH, entity);
        record.store();
    }

    /**
     * 更新
     */
    public void update(FeeCalBatchEntity entity) {
        dslContext.update(FEE_CAL_BATCH)
                .set(FEE_CAL_BATCH.STATUS, entity.getStatus())
                .set(FEE_CAL_BATCH.BILLING_DATA_STATUS, entity.getBillingDataStatus())
                .set(FEE_CAL_BATCH.DEPOSIT_BALANCE_TOTAL, entity.getDepositBalanceTotal())
                .set(FEE_CAL_BATCH.DEPOSIT_AMOUNT_DEDUCT, entity.getDepositAmountDeduct())
                .set(FEE_CAL_BATCH.DEPOSIT_BALANCE_REMAIN, entity.getDepositBalanceRemain())
                .set(FEE_CAL_BATCH.UNPAID_AMOUNT_REMAIN, entity.getUnpaidAmountRemain())
                .where(FEE_CAL_BATCH.BATCH_NO.eq(entity.getBatchNo()))
                .execute();
    }

    /**
     * 更新状态
     */
    public void updateStatus(String batchNo, String status) {
        dslContext.update(FEE_CAL_BATCH)
                .set(FEE_CAL_BATCH.STATUS, status)
                .where(FEE_CAL_BATCH.BATCH_NO.eq(batchNo))
                .execute();
    }

    /**
     * 更新账单数据状态
     */
    public void updateBillingStatus(String batchNo, String billingStatus) {
        dslContext.update(FEE_CAL_BATCH)
                .set(FEE_CAL_BATCH.BILLING_DATA_STATUS, billingStatus)
                .where(FEE_CAL_BATCH.BATCH_NO.eq(batchNo))
                .execute();
    }
}
