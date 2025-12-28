package com.merchant.feecal.dto.fund;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 回调请求
 */
@Data
public class FundInstructionCallbackRequest {
    @NotBlank
    private String operator;
    private String operatorName;
    private String remark;
}
