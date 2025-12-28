package com.merchant.feecal.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 费用清算页面数据DTO
 * 统一页面数据结构（所有渲染/计算/保存/提交的出参完全一致）
 */
@Data
public class FeeCalPageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 批次状态: EDITING / DONE
     */
    private String batchStatus;

    /**
     * 账单数据状态：PENDING / LOADING / READY / FAILED
     */
    private String billingDataStatus;

    /**
     * 清算主体信息
     */
    private MerchantInfo merchantInfo;

    /**
     * 保证金信息
     */
    private DepositInfo depositInfo;

    /**
     * 费用项列表
     */
    private List<TermCard> termCards;

    @Data
    public static class MerchantInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private String merchantType;
        private String merchantCode;
    }

    @Data
    public static class DepositInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * 初始保证金余额（用户输入）
         */
        private BigDecimal depositBalanceTotal;
        /**
         * 清算后剩余保证金（计算得出）
         */
        private BigDecimal depositBalanceRemain;
        /**
         * 本次抵扣金额总和
         */
        private BigDecimal depositAmountDeduct;
        /**
         * 抵扣后剩余欠付金额（仍需支付）
         */
        private BigDecimal unpaidAmountRemain;
    }

    @Data
    public static class TermCard implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * 费用项 code
         */
        private String code;
        /**
         * 费用项名称
         */
        private String name;
        /**
         * 状态: PENDING / DRAFT / DONE
         */
        private String status;
        /**
         * 是否由账单自动填充
         */
        private Boolean autoLoad;
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
         * 是否支持账单级分配
         */
        private Boolean supportBillAlloc;
        /**
         * 账单明细
         */
        private List<FeeCalBillItemDTO> billItems;
    }
}

