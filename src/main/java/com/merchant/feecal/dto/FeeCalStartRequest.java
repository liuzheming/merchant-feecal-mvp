package com.merchant.feecal.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 发起清算请求DTO
 */
@Data
public class FeeCalStartRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主体类型
     */
    private String merchantType;

    /**
     * 主体编码
     */
    private String merchantCode;
}


