package com.merchant.feecal.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 发起清算响应DTO
 */
@Data
public class FeeCalStartResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 批次号
     */
    private String batchNo;
}


