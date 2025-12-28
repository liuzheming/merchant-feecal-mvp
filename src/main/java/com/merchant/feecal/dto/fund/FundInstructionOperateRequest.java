package com.merchant.feecal.dto.fund;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 执行/重试请求
 */
@Data
public class FundInstructionOperateRequest {
    @NotBlank
    private String operator;
    private String operatorName;
    private String remark;
    private List<String> attachmentIds;
}
