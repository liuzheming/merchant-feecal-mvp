package com.merchant.feecal.repo;

import com.merchant.dao.tables.FeeCalTermInstAlloc;
import com.merchant.dao.tables.pojos.FeeCalTermInstAllocEntity;
import com.merchant.dao.tables.records.FeeCalTermInstAllocRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * fee_cal_term_inst_alloc 表访问
 */
@Component
public class FeeCalTermInstAllocRepo {

    @Resource
    private DSLContext dslContext;

    public void deleteByTermInstId(Long termInstId) {
        dslContext.deleteFrom(FeeCalTermInstAlloc.FEE_CAL_TERM_INST_ALLOC)
                .where(FeeCalTermInstAlloc.FEE_CAL_TERM_INST_ALLOC.TERM_INST_ID.eq(termInstId))
                .execute();
    }

    public void deleteByTermInstIds(Collection<Long> termInstIds) {
        if (termInstIds == null || termInstIds.isEmpty()) {
            return;
        }
        dslContext.deleteFrom(FeeCalTermInstAlloc.FEE_CAL_TERM_INST_ALLOC)
                .where(FeeCalTermInstAlloc.FEE_CAL_TERM_INST_ALLOC.TERM_INST_ID.in(termInstIds))
                .execute();
    }

    public void batchInsert(List<FeeCalTermInstAllocEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        for (FeeCalTermInstAllocEntity entity : entities) {
            FeeCalTermInstAllocRecord record = dslContext.newRecord(FeeCalTermInstAlloc.FEE_CAL_TERM_INST_ALLOC, entity);
            record.store();
        }
    }

    public List<FeeCalTermInstAllocEntity> listByTermInstIds(Collection<Long> termInstIds) {
        if (termInstIds == null || termInstIds.isEmpty()) {
            return Collections.emptyList();
        }
        return dslContext.selectFrom(FeeCalTermInstAlloc.FEE_CAL_TERM_INST_ALLOC)
                .where(FeeCalTermInstAlloc.FEE_CAL_TERM_INST_ALLOC.TERM_INST_ID.in(termInstIds))
                .fetchInto(FeeCalTermInstAllocEntity.class);
    }

    public List<FeeCalTermInstAllocEntity> listByTermInstId(Long termInstId) {
        return dslContext.selectFrom(FeeCalTermInstAlloc.FEE_CAL_TERM_INST_ALLOC)
                .where(FeeCalTermInstAlloc.FEE_CAL_TERM_INST_ALLOC.TERM_INST_ID.eq(termInstId))
                .fetchInto(FeeCalTermInstAllocEntity.class);
    }

    public void updateAllocation(Long id, boolean allocFlag, BigDecimal allocAmount) {
        dslContext.update(FeeCalTermInstAlloc.FEE_CAL_TERM_INST_ALLOC)
                .set(FeeCalTermInstAlloc.FEE_CAL_TERM_INST_ALLOC.ALLOC_FLAG, allocFlag ? 1 : 0)
                .set(FeeCalTermInstAlloc.FEE_CAL_TERM_INST_ALLOC.ALLOC_AMOUNT, allocAmount)
                .where(FeeCalTermInstAlloc.FEE_CAL_TERM_INST_ALLOC.ID.eq(id))
                .execute();
    }
}
