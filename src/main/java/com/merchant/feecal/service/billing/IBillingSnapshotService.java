package com.merchant.feecal.service.billing;

import com.merchant.dao.tables.pojos.FeeCalBillingSnapshotEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 账单快照访问接口（纯数据操作）
 */
public interface IBillingSnapshotService {

    /**
     * 查询指定批次及 termCode 的账单快照
     */
    List<FeeCalBillingSnapshotEntity> listBillingSnapshot(String batchNo, String termCode);

    /**
     * 用最新账单快照覆盖批次
     */
    void replaceBatch(String batchNo, List<FeeCalBillingSnapshotEntity> entities);

    /**
     * 删除批次账单快照
     */
    void deleteByBatchNo(String batchNo);

    /**
     * 统计批次中各 term 的欠费金额
     */
    Map<String, BigDecimal> aggregateUnpaidAmount(String batchNo);
}
