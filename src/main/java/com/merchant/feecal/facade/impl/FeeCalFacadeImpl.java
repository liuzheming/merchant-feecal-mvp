package com.merchant.feecal.facade.impl;

import com.merchant.feecal.controller.response.PageResult;
import com.merchant.feecal.dto.*;
import com.merchant.feecal.facade.IFeeCalFacade;
import com.merchant.feecal.service.batch.IFeeCalBatchQueryService;
import com.merchant.feecal.service.core.IFeeCalCoreService;
import com.merchant.feecal.service.fund.IFundInstructionAppService;
import com.merchant.feecal.service.summary.IFeeCalSummaryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 默认流程集成 Facade，当前直接委派给领域服务
 */
@Service
public class FeeCalFacadeImpl implements IFeeCalFacade {

    @Resource
    private IFeeCalCoreService feeCalCoreService;
    @Resource
	private IFeeCalSummaryService feeCalSummaryService;
    @Resource
    private IFeeCalBatchQueryService feeCalBatchQueryService;
    @Resource
    private IFundInstructionAppService fundInstructionAppService;

    @Override
    public FeeCalStartResponse start(FeeCalStartRequest request) {
        return feeCalCoreService.start(request);
    }

    @Override
    public FeeCalPageDTO summaryPage(String batchNo) {
        return feeCalSummaryService.getPage(batchNo);
    }

    @Override
    public FeeCalPageDTO summaryCalculate(FeeCalCalculateRequest request) {
        return feeCalSummaryService.calculate(request);
    }

    @Override
    public FeeCalPageDTO summarySaveDraft(FeeCalSaveRequest request) {
        return feeCalSummaryService.saveDraft(request);
    }

    @Override
    public FeeCalPageDTO summarySubmit(FeeCalSubmitRequest request) {
        FeeCalPageDTO pageDTO = feeCalSummaryService.submit(request);
        fundInstructionAppService.generateForBatch(request.getBatchNo());
        return pageDTO;
    }

    @Override
    public PageResult<FeeCalBatchListItemDTO> batchList(FeeCalBatchListRequest request) {
        return feeCalBatchQueryService.listBatches(request);
    }
}
