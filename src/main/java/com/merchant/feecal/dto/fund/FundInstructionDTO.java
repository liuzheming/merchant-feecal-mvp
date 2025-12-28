package com.merchant.feecal.dto.fund;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资金指令 DTO
 */
@Data
public class FundInstructionDTO {
    private Long id;
    private String batchNo;
    private Long termInstId;

    private String settleSubjectType;
    private String settleSubjectNo;

    private String payerType;
    private String payerNo;
    private String payeeType;
    private String payeeNo;

    private String fundDirection;
    private String fundBizType;
    private String accountType;

    private BigDecimal shouldAmount;
    private BigDecimal actualAmount;

    private String fundStatus;
    private String callbackStatus;

    private String fundOrderId;
    private String fundChannel;
    private String fundOrderInfo;

    private String attachmentIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
