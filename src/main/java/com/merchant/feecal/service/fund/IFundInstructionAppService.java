package com.merchant.feecal.service.fund;

import com.merchant.feecal.dto.fund.FundInstructionCallbackRequest;
import com.merchant.feecal.dto.fund.FundInstructionDTO;
import com.merchant.feecal.dto.fund.FundInstructionOperateRequest;

import java.util.List;

/**
 * 资金指令应用服务
 */
public interface IFundInstructionAppService {

    List<FundInstructionDTO> generateForBatch(String batchNo);

    List<FundInstructionDTO> listByBatch(String batchNo);

    FundInstructionDTO execute(Long instructionId, FundInstructionOperateRequest request);

    FundInstructionDTO retry(Long instructionId, FundInstructionOperateRequest request);

    FundInstructionDTO callback(Long instructionId, FundInstructionCallbackRequest request);
}
