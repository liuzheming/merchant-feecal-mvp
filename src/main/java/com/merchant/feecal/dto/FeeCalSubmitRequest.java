package com.merchant.feecal.dto;

import com.merchant.feecal.service.TermCardInput;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 提交清算请求DTO
 */
@Data
public class FeeCalSubmitRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 保证金信息
     */
    private DepositInfo depositInfo;

    /**
     * 费用项列表
     */
    private List<TermCard> termCards;

    @Data
    public static class DepositInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * 初始保证金余额（用户输入）
         */
        private BigDecimal depositBalanceTotal;
    }

    @Data
    public static class TermCard implements Serializable, TermCardInput {
        private static final long serialVersionUID = 1L;
        /**
         * 费用项 code
         */
        private String code;
        /**
         * 欠付金额
         */
        private BigDecimal unpaidAmount;
        /**
         * 是否可抵扣
         */
        private Boolean deductFlag;
        /**
         * 抵扣金额
         */
        private BigDecimal deductAmount;
        /**
         * 账单分配
         */
        private List<FeeCalBillItemDTO> billItems;
    }
}
