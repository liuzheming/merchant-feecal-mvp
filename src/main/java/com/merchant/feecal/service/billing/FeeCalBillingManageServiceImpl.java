package com.merchant.feecal.service.billing;

import com.merchant.common.exception.ServiceException;
import com.merchant.dao.tables.pojos.FeeCalBatchEntity;
import com.merchant.dao.tables.pojos.FeeCalBillingSnapshotEntity;
import com.merchant.dao.tables.pojos.FeeCalMerchantEntity;
import com.merchant.feecal.adapter.billing.BillingAdapter;
import com.merchant.feecal.repo.FeeCalBatchRepo;
import com.merchant.feecal.service.FeeCalConstants.Status.BillingData;
import com.merchant.feecal.service.alloc.FeeCalTermInstAllocService;
import com.merchant.feecal.service.core.FeeCalCoreContext;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 账单拉取流程管理实现
 */
@Service
public class FeeCalBillingManageServiceImpl implements IFeeCalBillingManageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeeCalBillingManageServiceImpl.class);

    @Resource
    private BillingAdapter billingAdapter;

    @Resource
    private FeeCalBatchRepo feeCalBatchRepo;

    @Resource
    private IBillingSnapshotService billingSnapshotService;
    @Resource
    private FeeCalTermInstAllocService termInstAllocService;

    @Override
    public void refreshBillingData(FeeCalCoreContext coreContext) {
        if (coreContext == null) {
            throw new ServiceException("账单拉取上下文不完整");
        }
        FeeCalBatchEntity batch = coreContext.getBatch();
        FeeCalMerchantEntity merchant = coreContext.getMerchant();
        if (batch == null || merchant == null) {
            throw new ServiceException("账单拉取上下文不完整");
        }
        String currentStatus = batch.getBillingDataStatus();
        if (!BillingData.PENDING.equals(currentStatus) && !BillingData.FAILED.equals(currentStatus)) {
            return;
        }

        feeCalBatchRepo.updateBillingStatus(batch.getBatchNo(), BillingData.LOADING);
        batch.setBillingDataStatus(BillingData.LOADING);
        try {
            BillingAdapter.BillingPullContext context = new BillingAdapter.BillingPullContext(
                    batch.getBatchNo(),
                    merchant.getMerchantType(),
                    merchant.getMerchantCode());
            List<FeeCalBillingSnapshotEntity> billingViews = billingAdapter.pullBillingViews(context);
            if (CollectionUtils.isEmpty(billingViews)) {
                billingViews = Collections.emptyList();
            }
            billingSnapshotService.replaceBatch(batch.getBatchNo(), billingViews);

            feeCalBatchRepo.updateBillingStatus(batch.getBatchNo(), BillingData.READY);
            batch.setBillingDataStatus(BillingData.READY);
            termInstAllocService.rebuildAllocations(coreContext);
        } catch (Exception ex) {
            LOGGER.error("refresh billing data failed, batchNo={}", batch.getBatchNo(), ex);
            feeCalBatchRepo.updateBillingStatus(batch.getBatchNo(), BillingData.FAILED);
            batch.setBillingDataStatus(BillingData.FAILED);
            throw new ServiceException("拉取账单失败，请稍后重试", ex);
        }
    }
}
