package com.merchant.feecal.service.fund;

import com.merchant.common.exception.ServiceException;
import com.merchant.dao.tables.pojos.FeeCalFundInstructionEntity;
import com.merchant.feecal.repo.FundInstructionRepo;
import com.merchant.feecal.service.FeeCalConstants.Status.FundCallback;
import com.merchant.feecal.service.FeeCalConstants.Status.FundInstruction;
import com.merchant.feecal.service.fund.model.FundExecutionContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 默认计费回调实现（MVP 直接置成功）
 */
@Service
public class FundBillingCallbackServiceImpl implements IFundBillingCallbackService {

    @Resource
    private FundInstructionRepo fundInstructionRepo;

    @Override
    public void callback(FeeCalFundInstructionEntity instruction, FundExecutionContext context) {
        if (instruction == null) {
            throw new ServiceException("资金指令不存在");
        }
        if (!FundInstruction.SUCCESS.equals(instruction.getFundStatus())) {
            throw new ServiceException("资金未成功，不能回写计费状态");
        }
        if (FundCallback.SUCCESS.equals(instruction.getCallbackStatus())) {
            return;
        }
        fundInstructionRepo.updateCallbackStatus(instruction.getId(), FundCallback.SUCCESS);
    }
}
