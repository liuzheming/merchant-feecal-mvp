package com.merchant.feecal.controller;

import com.merchant.feecal.controller.response.ResponseResult;
import com.merchant.feecal.dto.*;
import com.merchant.feecal.facade.IFeeCalFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * 费用清算 Controller
 */
@Slf4j
@RestController
@RequestMapping("/web/feeCal/summary")
public class FeeCalSummaryController {

    private static final String TIP_DEPOSIT_INSUFFICIENT = "保证金余额不足";

    @Resource
    private IFeeCalFacade feeCalFacade;

    /**
     * 1）发起清算（创建批次）
     */
    @PostMapping("/start")
    public ResponseResult<FeeCalStartResponse> start(@RequestBody FeeCalStartRequest request) {
        FeeCalStartResponse response = feeCalFacade.start(request);
        return ResponseResult.success(response);
    }

    /**
     * 自动清算：start -> submit -> 生成指令 -> 自动执行
     */
    @PostMapping("/autoStart")
    public ResponseResult<FeeCalAutoStartResponse> autoStart(@RequestBody FeeCalStartRequest request) {
        FeeCalAutoStartResponse response = feeCalFacade.autoStart(request);
        return ResponseResult.success(response);
    }

    /**
     * 2）渲染清算页面（根据 batchNo 查询）
     */
    @GetMapping("/page")
    public ResponseResult<FeeCalPageDTO> getPage(@RequestParam String batchNo) {
        FeeCalPageDTO pageDTO = feeCalFacade.summaryPage(batchNo);
        return buildPageResponse(pageDTO);
    }

    /**
     * 3）实时计算接口（不落库）
     */
    @PostMapping("/calculate")
    public ResponseResult<FeeCalPageDTO> calculate(@RequestBody FeeCalCalculateRequest request) {
        FeeCalPageDTO pageDTO = feeCalFacade.summaryCalculate(request);
        return buildPageResponse(pageDTO);
    }

    /**
     * 4）保存草稿（落库）
     */
    @PostMapping("/saveDraft")
    public ResponseResult<FeeCalPageDTO> saveDraft(@RequestBody FeeCalSaveRequest request) {
        FeeCalPageDTO pageDTO = feeCalFacade.summarySaveDraft(request);
        return buildPageResponse(pageDTO);
    }

    /**
     * 5）提交清算（落库 + DONE）
     */
    @PostMapping("/submit")
    public ResponseResult<FeeCalPageDTO> submit(@RequestBody FeeCalSubmitRequest request) {
        FeeCalPageDTO pageDTO = feeCalFacade.summarySubmit(request);
        return buildPageResponse(pageDTO);
    }

    private ResponseResult<FeeCalPageDTO> buildPageResponse(FeeCalPageDTO pageDTO) {
        ResponseResult<FeeCalPageDTO> responseResult = ResponseResult.success(pageDTO);
        if (pageDTO != null
                && pageDTO.getDepositInfo() != null
                && pageDTO.getDepositInfo().getDepositBalanceRemain() != null
                && pageDTO.getDepositInfo().getDepositBalanceRemain().compareTo(BigDecimal.ZERO) < 0) {
            responseResult.setTip(TIP_DEPOSIT_INSUFFICIENT);
        }
        return responseResult;
    }
}
