package com.merchant.feecal.dto;

import lombok.Data;

/**
 * 批次列表查询入参
 */
@Data
public class FeeCalBatchListRequest {

    private String merchantCode;
    private String merchantType;
    private String batchStatus;
    private String billingDataStatus;
    private Integer pageNo;
    private Integer pageSize;
}
