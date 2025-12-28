package com.merchant.feecal.repo;

import com.merchant.dao.tables.pojos.FeeCalBillingSnapshotEntity;
import com.merchant.dao.tables.records.FeeCalBillingSnapshotRecord;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.merchant.dao.Tables.FEE_CAL_BILLING_SNAPSHOT;

/**
 * 账单快照表 Repository
 */
@Component
public class FeeCalBillingSnapshotRepo {

    @Resource
    private DSLContext dslContext;

    /**
     * 替换某批次的快照：先删再插
     */
    public void replaceBatch(String batchNo, List<FeeCalBillingSnapshotEntity> entities) {
        deleteByBatchNo(batchNo);
        batchInsert(entities);
    }

    /**
     * 删除批次快照
     */
    public void deleteByBatchNo(String batchNo) {
        dslContext.deleteFrom(FEE_CAL_BILLING_SNAPSHOT)
                .where(FEE_CAL_BILLING_SNAPSHOT.BATCH_NO.eq(batchNo))
                .execute();
    }

    /**
     * 批量插入
     */
    public void batchInsert(List<FeeCalBillingSnapshotEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        for (FeeCalBillingSnapshotEntity entity : entities) {
            FeeCalBillingSnapshotRecord record = dslContext.newRecord(FEE_CAL_BILLING_SNAPSHOT, entity);
            record.store();
        }
    }

    /**
     * 按批次查询所有账单快照
     */
    public List<FeeCalBillingSnapshotEntity> queryByBatchNo(String batchNo) {
        return dslContext.selectFrom(FEE_CAL_BILLING_SNAPSHOT)
                .where(FEE_CAL_BILLING_SNAPSHOT.BATCH_NO.eq(batchNo))
                .fetchInto(FeeCalBillingSnapshotEntity.class);
    }

    /**
     * 按批次+termCode 查询账单快照
     */
    public List<FeeCalBillingSnapshotEntity> queryByBatchNoAndTermCode(String batchNo, String termCode) {
        return dslContext.selectFrom(FEE_CAL_BILLING_SNAPSHOT)
                .where(FEE_CAL_BILLING_SNAPSHOT.BATCH_NO.eq(batchNo))
                .and(FEE_CAL_BILLING_SNAPSHOT.TERM_CODE.eq(termCode))
                .fetchInto(FeeCalBillingSnapshotEntity.class);
    }

    /**
     * 根据 ID 集合查询
     */
    public List<FeeCalBillingSnapshotEntity> listByIds(java.util.Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return dslContext.selectFrom(FEE_CAL_BILLING_SNAPSHOT)
                .where(FEE_CAL_BILLING_SNAPSHOT.ID.in(ids))
                .fetchInto(FeeCalBillingSnapshotEntity.class);
    }

    /**
     * 统计每个 termCode 的未付金额总和
     */
    public Map<String, BigDecimal> aggregateUnpaidAmount(String batchNo) {
        List<Record2<String, BigDecimal>> records = dslContext
                .select(FEE_CAL_BILLING_SNAPSHOT.TERM_CODE,
                        DSL.sum(FEE_CAL_BILLING_SNAPSHOT.BILLING_UNPAID_AMOUNT))
                .from(FEE_CAL_BILLING_SNAPSHOT)
                .where(FEE_CAL_BILLING_SNAPSHOT.BATCH_NO.eq(batchNo))
                .groupBy(FEE_CAL_BILLING_SNAPSHOT.TERM_CODE)
                .fetch();
        if (records == null) {
            return Collections.emptyMap();
        }
        return records.stream()
                .collect(Collectors.toMap(
                        Record2::value1,
                        record -> {
                            BigDecimal value = record.value2();
                            return value == null ? BigDecimal.ZERO : value;
                        }));
    }
}
