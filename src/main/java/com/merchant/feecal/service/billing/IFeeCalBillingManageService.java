package com.merchant.feecal.service.billing;

import com.merchant.feecal.service.core.FeeCalCoreContext;

/**
 * 账单拉取流程管理接口
 */
public interface IFeeCalBillingManageService {

    /**
     * 在 PENDING/FAILED 状态下刷新账单快照
     */
    void refreshBillingData(FeeCalCoreContext coreContext);
}
