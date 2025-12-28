package com.merchant.feecal.controller;

import com.merchant.feecal.controller.response.ResponseResult;
import com.merchant.feecal.dto.fund.FundInstructionCallbackRequest;
import com.merchant.feecal.dto.fund.FundInstructionDTO;
import com.merchant.feecal.dto.fund.FundInstructionOperateRequest;
import com.merchant.feecal.service.fund.IFundInstructionAppService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 资金指令相关接口
 */
@Validated
@RestController
@RequestMapping("/web/feeCal/fund")
public class FundInstructionController {

    @Resource
    private IFundInstructionAppService fundInstructionAppService;

    @PostMapping("/batch/{batchNo}/generate")
    public ResponseResult<List<FundInstructionDTO>> generate(@PathVariable String batchNo) {
        return ResponseResult.success(fundInstructionAppService.generateForBatch(batchNo));
    }

    @GetMapping("/batch/{batchNo}/list")
    public ResponseResult<List<FundInstructionDTO>> list(@PathVariable String batchNo) {
        return ResponseResult.success(fundInstructionAppService.listByBatch(batchNo));
    }

    @PostMapping("/{instructionId}/execute")
    public ResponseResult<FundInstructionDTO> execute(@PathVariable Long instructionId,
                                                      @Valid @RequestBody FundInstructionOperateRequest request) {
        return ResponseResult.success(fundInstructionAppService.execute(instructionId, request));
    }

    @PostMapping("/{instructionId}/retry")
    public ResponseResult<FundInstructionDTO> retry(@PathVariable Long instructionId,
                                                    @Valid @RequestBody FundInstructionOperateRequest request) {
        return ResponseResult.success(fundInstructionAppService.retry(instructionId, request));
    }

    @PostMapping("/{instructionId}/callback")
    public ResponseResult<FundInstructionDTO> callback(@PathVariable Long instructionId,
                                                       @Valid @RequestBody FundInstructionCallbackRequest request) {
        return ResponseResult.success(fundInstructionAppService.callback(instructionId, request));
    }
}
