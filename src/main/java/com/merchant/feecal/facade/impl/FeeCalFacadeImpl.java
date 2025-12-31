package com.merchant.feecal.facade.impl;

import com.merchant.feecal.controller.response.PageResult;
import com.merchant.feecal.dto.*;
import com.merchant.feecal.facade.IFeeCalFacade;
import com.merchant.common.exception.ServiceException;
import com.merchant.feecal.service.FeeCalConstants;
import com.merchant.feecal.service.batch.IFeeCalBatchQueryService;
import com.merchant.feecal.service.core.IFeeCalCoreService;
import com.merchant.feecal.service.fund.IFundInstructionAppService;
import com.merchant.feecal.dto.fund.FundInstructionDTO;
import com.merchant.feecal.dto.fund.FundInstructionOperateRequest;
import com.merchant.feecal.service.summary.IFeeCalSummaryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

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
    private static final String AUTO_OPERATOR = "system";
    private static final String AUTO_OPERATOR_NAME = "system";

    @Override
    public FeeCalStartResponse start(FeeCalStartRequest request) {
        return feeCalCoreService.start(request);
    }

    @Override
    public FeeCalAutoStartResponse autoStart(FeeCalStartRequest request) {
        FeeCalStartResponse startResponse = feeCalCoreService.start(request);
        String batchNo = startResponse.getBatchNo();

        FeeCalPageDTO pageDTO = feeCalSummaryService.getPage(batchNo);
        if (pageDTO == null) {
            throw new ServiceException("自动清算失败：页面数据为空");
        }
        if (!FeeCalConstants.Status.BillingData.READY.equals(pageDTO.getBillingDataStatus())) {
            throw new ServiceException("自动清算失败：账单未就绪");
        }

        FeeCalSubmitRequest submitRequest = buildAutoSubmitRequest(pageDTO);
        feeCalSummaryService.submit(submitRequest);

        fundInstructionAppService.generateForBatch(batchNo);
        List<FundInstructionDTO> instructions = fundInstructionAppService.listByBatch(batchNo);
        for (FundInstructionDTO instruction : instructions) {
            if (FeeCalConstants.Status.FundInstruction.PENDING.equals(instruction.getFundStatus())) {
                FundInstructionOperateRequest executeRequest = new FundInstructionOperateRequest();
                executeRequest.setOperator(AUTO_OPERATOR);
                executeRequest.setOperatorName(AUTO_OPERATOR_NAME);
                executeRequest.setRemark("auto execute");
                fundInstructionAppService.execute(instruction.getId(), executeRequest);
            }
        }

        FeeCalAutoStartResponse response = new FeeCalAutoStartResponse();
        response.setBatchNo(batchNo);
        response.setFundInstructions(fundInstructionAppService.listByBatch(batchNo));
        return response;
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

    private FeeCalSubmitRequest buildAutoSubmitRequest(FeeCalPageDTO pageDTO) {
        FeeCalSubmitRequest submitRequest = new FeeCalSubmitRequest();
        submitRequest.setBatchNo(pageDTO.getBatchNo());

        FeeCalSubmitRequest.DepositInfo depositInfo = new FeeCalSubmitRequest.DepositInfo();
        if (pageDTO.getDepositInfo() != null) {
            depositInfo.setDepositBalanceTotal(pageDTO.getDepositInfo().getDepositBalanceTotal());
        }
        submitRequest.setDepositInfo(depositInfo);

        if (pageDTO.getTermCards() == null) {
            submitRequest.setTermCards(java.util.Collections.emptyList());
            return submitRequest;
        }

        List<FeeCalSubmitRequest.TermCard> termCards = pageDTO.getTermCards().stream().map(card -> {
            FeeCalSubmitRequest.TermCard submitCard = new FeeCalSubmitRequest.TermCard();
            submitCard.setCode(card.getCode());
            submitCard.setUnpaidAmount(card.getUnpaidAmount());
            submitCard.setDeductFlag(false);
            submitCard.setDeductAmount(BigDecimal.ZERO);
            submitCard.setBillItems(card.getBillItems());
            return submitCard;
        }).collect(Collectors.toList());
        submitRequest.setTermCards(termCards);
        return submitRequest;
    }
}
