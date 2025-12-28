package com.merchant.feecal.service.batch;

import com.merchant.feecal.controller.response.PageResult;
import com.merchant.feecal.dto.FeeCalBatchListItemDTO;
import com.merchant.feecal.dto.FeeCalBatchListRequest;

/**
 * 批次列表查询服务
 */
public interface IFeeCalBatchQueryService {

    PageResult<FeeCalBatchListItemDTO> listBatches(FeeCalBatchListRequest request);
}
