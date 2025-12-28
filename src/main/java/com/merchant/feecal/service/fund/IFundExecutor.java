package com.merchant.feecal.service.fund;

import com.merchant.dao.tables.pojos.FeeCalFundInstructionEntity;
import com.merchant.feecal.service.fund.model.FundExecutionContext;

/**
 * 资金执行器
 */
public interface IFundExecutor {

    /**
     * 执行资金指令
     */
    void execute(FeeCalFundInstructionEntity instruction, FundExecutionContext context);
}
