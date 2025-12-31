package com.merchant.feecal.repo;

import com.merchant.dao.tables.pojos.FeeCalIdempotentEntity;
import com.merchant.dao.tables.records.FeeCalIdempotentRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.merchant.dao.Tables.FEE_CAL_IDEMPOTENT;

@Component
public class FeeCalIdempotentRepo {

    @Resource
    private DSLContext dslContext;

    public FeeCalIdempotentEntity queryByBizAndRequestId(String bizType, String requestId) {
        return dslContext.selectFrom(FEE_CAL_IDEMPOTENT)
                .where(FEE_CAL_IDEMPOTENT.BIZ_TYPE.eq(bizType))
                .and(FEE_CAL_IDEMPOTENT.REQUEST_ID.eq(requestId))
                .fetchOneInto(FeeCalIdempotentEntity.class);
    }

    public void insert(FeeCalIdempotentEntity entity) {
        FeeCalIdempotentRecord record = dslContext.newRecord(FEE_CAL_IDEMPOTENT, entity);
        record.store();
    }

    public void updateStatusAndBatchNo(Long id, String status, String batchNo, String resultMsg) {
        dslContext.update(FEE_CAL_IDEMPOTENT)
                .set(FEE_CAL_IDEMPOTENT.STATUS, status)
                .set(FEE_CAL_IDEMPOTENT.BATCH_NO, batchNo)
                .set(FEE_CAL_IDEMPOTENT.RESULT_MSG, resultMsg)
                .where(FEE_CAL_IDEMPOTENT.ID.eq(id))
                .execute();
    }

    public void updateStatus(Long id, String status, String resultMsg) {
        dslContext.update(FEE_CAL_IDEMPOTENT)
                .set(FEE_CAL_IDEMPOTENT.STATUS, status)
                .set(FEE_CAL_IDEMPOTENT.RESULT_MSG, resultMsg)
                .where(FEE_CAL_IDEMPOTENT.ID.eq(id))
                .execute();
    }
}
