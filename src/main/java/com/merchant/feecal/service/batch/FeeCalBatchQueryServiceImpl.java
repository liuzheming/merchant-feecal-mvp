package com.merchant.feecal.service.batch;

import com.merchant.feecal.controller.response.PageResult;
import com.merchant.feecal.dto.FeeCalBatchListItemDTO;
import com.merchant.feecal.dto.FeeCalBatchListRequest;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static com.merchant.dao.Tables.FEE_CAL_BATCH;
import static com.merchant.dao.Tables.FEE_CAL_MERCHANT;

/**
 * 批次列表查询实现
 */
@Service
public class FeeCalBatchQueryServiceImpl implements IFeeCalBatchQueryService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    @Resource
    private DSLContext dslContext;

    @Override
    public PageResult<FeeCalBatchListItemDTO> listBatches(FeeCalBatchListRequest request) {
        int pageNo = request.getPageNo() == null || request.getPageNo() < 1 ? 1 : request.getPageNo();
        int size = request.getPageSize() == null || request.getPageSize() < 1
                ? DEFAULT_PAGE_SIZE
                : Math.min(request.getPageSize(), MAX_PAGE_SIZE);
        int offset = (pageNo - 1) * size;

        Condition condition = DSL.trueCondition();
        if (StringUtils.isNotBlank(request.getBatchStatus())) {
            condition = condition.and(FEE_CAL_BATCH.STATUS.eq(request.getBatchStatus()));
        }
        if (StringUtils.isNotBlank(request.getBillingDataStatus())) {
            condition = condition.and(FEE_CAL_BATCH.BILLING_DATA_STATUS.eq(request.getBillingDataStatus()));
        }
        if (StringUtils.isNotBlank(request.getMerchantCode())) {
            condition = condition.and(FEE_CAL_MERCHANT.MERCHANT_CODE.eq(request.getMerchantCode()));
        }
        if (StringUtils.isNotBlank(request.getMerchantType())) {
            condition = condition.and(FEE_CAL_MERCHANT.MERCHANT_TYPE.eq(request.getMerchantType()));
        }

        int total = dslContext.selectCount()
                .from(FEE_CAL_BATCH)
                .join(FEE_CAL_MERCHANT).on(FEE_CAL_BATCH.BATCH_NO.eq(FEE_CAL_MERCHANT.BATCH_NO))
                .where(condition)
                .fetchOne(0, Integer.class);

        List<FeeCalBatchListItemDTO> items = dslContext
                .select(
                        FEE_CAL_BATCH.BATCH_NO,
                        FEE_CAL_BATCH.STATUS,
                        FEE_CAL_BATCH.BILLING_DATA_STATUS,
                        FEE_CAL_BATCH.DEPOSIT_AMOUNT_DEDUCT,
                        FEE_CAL_BATCH.DEPOSIT_BALANCE_REMAIN,
                        FEE_CAL_BATCH.UNPAID_AMOUNT_REMAIN,
                        FEE_CAL_BATCH.CTIME,
                        FEE_CAL_MERCHANT.MERCHANT_TYPE,
                        FEE_CAL_MERCHANT.MERCHANT_CODE
                )
                .from(FEE_CAL_BATCH)
                .join(FEE_CAL_MERCHANT).on(FEE_CAL_BATCH.BATCH_NO.eq(FEE_CAL_MERCHANT.BATCH_NO))
                .where(condition)
                .orderBy(FEE_CAL_BATCH.CTIME.desc())
                .limit(size)
                .offset(offset)
                .fetch(this::mapRecord);

        int safeTotal =  total;
        return PageResult.of(pageNo, size, safeTotal, items);
    }

    private FeeCalBatchListItemDTO mapRecord(Record record) {
        FeeCalBatchListItemDTO item = new FeeCalBatchListItemDTO();
        item.setBatchNo(record.get(FEE_CAL_BATCH.BATCH_NO));
        item.setBatchStatus(record.get(FEE_CAL_BATCH.STATUS));
        item.setBillingDataStatus(record.get(FEE_CAL_BATCH.BILLING_DATA_STATUS));
        item.setDepositAmountDeduct(record.get(FEE_CAL_BATCH.DEPOSIT_AMOUNT_DEDUCT));
        item.setDepositBalanceRemain(record.get(FEE_CAL_BATCH.DEPOSIT_BALANCE_REMAIN));
        item.setUnpaidAmountRemain(record.get(FEE_CAL_BATCH.UNPAID_AMOUNT_REMAIN));
        item.setCreatedAt(record.get(FEE_CAL_BATCH.CTIME));
        item.setMerchantType(record.get(FEE_CAL_MERCHANT.MERCHANT_TYPE));
        item.setMerchantCode(record.get(FEE_CAL_MERCHANT.MERCHANT_CODE));
        return item;
    }
}
