package com.merchant.feecal.repo;

import com.merchant.dao.tables.pojos.FeeCalMerchantEntity;
import com.merchant.dao.tables.records.FeeCalMerchantRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.merchant.dao.Tables.FEE_CAL_MERCHANT;

/**
 * 清算主体表 Repository
 */
@Component
public class FeeCalMerchantRepo {

    @Resource
    private DSLContext dslContext;

    /**
     * 根据批次号查询
     */
    public List<FeeCalMerchantEntity> queryByBatchNo(String batchNo) {
        return dslContext.selectFrom(FEE_CAL_MERCHANT)
                .where(FEE_CAL_MERCHANT.BATCH_NO.eq(batchNo))
                .fetchInto(FeeCalMerchantEntity.class);
    }

    /**
     * 根据批次号和主体ID查询
     */
    public FeeCalMerchantEntity queryByBatchNoAndMerchantId(String batchNo, Long merchantId) {
        return dslContext.selectFrom(FEE_CAL_MERCHANT)
                .where(FEE_CAL_MERCHANT.BATCH_NO.eq(batchNo))
                .and(FEE_CAL_MERCHANT.ID.eq(merchantId))
                .fetchOneInto(FeeCalMerchantEntity.class);
    }

    /**
     * 插入
     */
    public Long insert(FeeCalMerchantEntity entity) {
        FeeCalMerchantRecord record = dslContext.newRecord(FEE_CAL_MERCHANT, entity);
        record.store();
        Long id = record.getId();
        entity.setId(id);
        return id;
    }
}

