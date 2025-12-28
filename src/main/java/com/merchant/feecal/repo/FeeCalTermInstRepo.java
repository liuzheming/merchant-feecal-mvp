package com.merchant.feecal.repo;

import com.merchant.dao.tables.pojos.FeeCalTermInstEntity;
import com.merchant.dao.tables.records.FeeCalTermInstRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.merchant.dao.Tables.FEE_CAL_TERM_INST;

/**
 * 费用项实例表 Repository
 */
@Component
public class FeeCalTermInstRepo {

    @Resource
    private DSLContext dslContext;

    /**
     * 根据批次号和主体ID查询
     */
    public List<FeeCalTermInstEntity> queryByBatchNoAndMerchantId(String batchNo, Long merchantId) {
        return dslContext.selectFrom(FEE_CAL_TERM_INST)
                .where(FEE_CAL_TERM_INST.BATCH_NO.eq(batchNo))
                .and(FEE_CAL_TERM_INST.MERCHANT_ID.eq(merchantId))
                .fetchInto(FeeCalTermInstEntity.class);
    }

    /**
     * 根据批次号查询
     */
    public List<FeeCalTermInstEntity> queryByBatchNo(String batchNo) {
        return dslContext.selectFrom(FEE_CAL_TERM_INST)
                .where(FEE_CAL_TERM_INST.BATCH_NO.eq(batchNo))
                .fetchInto(FeeCalTermInstEntity.class);
    }

    /**
     * 插入
     */
    public void insert(FeeCalTermInstEntity entity) {
        FeeCalTermInstRecord record = dslContext.newRecord(FEE_CAL_TERM_INST, entity);
        record.store();
    }

    /**
     * 批量插入
     */
    public void batchInsert(List<FeeCalTermInstEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        for (FeeCalTermInstEntity entity : entities) {
            insert(entity);
        }
    }

    /**
     * 更新
     */
    public void update(FeeCalTermInstEntity entity) {
        dslContext.update(FEE_CAL_TERM_INST)
                .set(FEE_CAL_TERM_INST.STATUS, entity.getStatus())
                .set(FEE_CAL_TERM_INST.UNPAID_AMOUNT, entity.getUnpaidAmount())
                .set(FEE_CAL_TERM_INST.DEDUCT_FLAG, entity.getDeductFlag())
                .set(FEE_CAL_TERM_INST.DEDUCT_AMOUNT, entity.getDeductAmount())
                .where(FEE_CAL_TERM_INST.ID.eq(entity.getId()))
                .execute();
    }

    /**
     * 根据主键查询
     */
    public FeeCalTermInstEntity findById(Long id) {
        return dslContext.selectFrom(FEE_CAL_TERM_INST)
                .where(FEE_CAL_TERM_INST.ID.eq(id))
                .fetchOneInto(FeeCalTermInstEntity.class);
    }

    /**
     * 批量更新状态
     */
    public void batchUpdateStatus(String batchNo, String status) {
        dslContext.update(FEE_CAL_TERM_INST)
                .set(FEE_CAL_TERM_INST.STATUS, status)
                .where(FEE_CAL_TERM_INST.BATCH_NO.eq(batchNo))
                .execute();
    }
}
