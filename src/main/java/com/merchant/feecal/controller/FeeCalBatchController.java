package com.merchant.feecal.controller;

import com.merchant.feecal.controller.response.PageResult;
import com.merchant.feecal.controller.response.ResponseResult;
import com.merchant.feecal.dto.FeeCalBatchListItemDTO;
import com.merchant.feecal.dto.FeeCalBatchListRequest;
import com.merchant.feecal.facade.IFeeCalFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 批次列表相关接口
 */
@RestController
@RequestMapping("/web/feeCal/batch")
public class FeeCalBatchController {

    @Resource
    private IFeeCalFacade feeCalFacade;

    @GetMapping("/list")
    public ResponseResult<PageResult<FeeCalBatchListItemDTO>> list(FeeCalBatchListRequest request) {
        return ResponseResult.success(feeCalFacade.batchList(request));
    }
}
