package com.merchant.feecal.service.fund;

/**
 * 资金指令生成器
 */
public interface IFundInstructionGenerator {

    /**
     * 根据批次生成资金指令（幂等）
     */
    void generateForBatch(String batchNo);
}
