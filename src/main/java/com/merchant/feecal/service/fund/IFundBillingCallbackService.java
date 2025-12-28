package com.merchant.feecal.service.fund;

import com.merchant.dao.tables.pojos.FeeCalFundInstructionEntity;
import com.merchant.feecal.service.fund.model.FundExecutionContext;

/**
 * 资金成功后的计费回调
 */
public interface IFundBillingCallbackService {

    void callback(FeeCalFundInstructionEntity instruction, FundExecutionContext context);
}
