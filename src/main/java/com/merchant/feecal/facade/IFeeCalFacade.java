package com.merchant.feecal.facade;

import com.merchant.feecal.controller.response.PageResult;
import com.merchant.feecal.dto.*;

/**
 * 流程集成层 Facade（M4）
 */
public interface IFeeCalFacade {

    FeeCalStartResponse start(FeeCalStartRequest request);
    FeeCalAutoStartResponse autoStart(FeeCalStartRequest request);


	/**
	 * summary 相关: 渲染清算汇总页面（根据 batchNo 查询）
	 */
    FeeCalPageDTO summaryPage(String batchNo);

    FeeCalPageDTO summaryCalculate(FeeCalCalculateRequest request);

    FeeCalPageDTO summarySaveDraft(FeeCalSaveRequest request);

    FeeCalPageDTO summarySubmit(FeeCalSubmitRequest request);

    PageResult<FeeCalBatchListItemDTO> batchList(FeeCalBatchListRequest request);
}
