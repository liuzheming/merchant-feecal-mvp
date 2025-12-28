package com.merchant.feecal.service.fund;

import com.merchant.common.exception.ServiceException;
import com.merchant.dao.tables.pojos.FeeCalFundInstructionEntity;
import com.merchant.feecal.repo.FundInstructionRepo;
import com.merchant.feecal.service.FeeCalConstants.Status.FundInstruction;
import com.merchant.feecal.service.fund.gateway.FundGateway;
import com.merchant.feecal.service.fund.gateway.FundGatewayRequest;
import com.merchant.feecal.service.fund.gateway.FundGatewayResponse;
import com.merchant.feecal.service.fund.model.FundExecutionContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * 默认资金执行器实现（调用 FundGateway 并回写状态）
 */
@Service
public class FundExecutorImpl implements IFundExecutor {

    @Resource
    private FundInstructionRepo fundInstructionRepo;
    @Resource
    private FundGateway fundGateway;
    @Override
    public void execute(FeeCalFundInstructionEntity instruction, FundExecutionContext context) {
        if (instruction == null) {
            throw new ServiceException("资金指令不存在");
        }
        if (context == null || StringUtils.isBlank(context.getOperator())) {
            throw new ServiceException("执行人不能为空");
        }
        FundGatewayRequest request = buildGatewayRequest(instruction, context);
        FundGatewayResponse response = null;
        try {
            fundInstructionRepo.updateFundStatus(instruction.getId(), FundInstruction.EXECUTING);
            response = fundGateway.execute(request);
            persistResult(instruction.getId(), response);
        } catch (Exception ex) {
            FundGatewayResponse errorResponse = FundGatewayResponse.builder()
                    .success(false)
                    .rawResponse(buildErrorMessage(ex))
                    .actualAmount(BigDecimal.ZERO)
                    .build();
            persistResult(instruction.getId(), errorResponse);
            throw new ServiceException("执行资金指令失败", ex);
        }
    }

    private FundGatewayRequest buildGatewayRequest(FeeCalFundInstructionEntity instruction, FundExecutionContext context) {
        return FundGatewayRequest.builder()
                .instructionId(instruction.getId())
                .batchNo(instruction.getBatchNo())
                .fundBizType(instruction.getFundBizType())
                .fundDirection(instruction.getFundDirection())
                .accountType(instruction.getAccountType())
                .shouldAmount(instruction.getShouldAmount())
                .payerType(instruction.getPayerType())
                .payerNo(instruction.getPayerNo())
                .payeeType(instruction.getPayeeType())
                .payeeNo(instruction.getPayeeNo())
                .operator(context.getOperator())
                .operatorName(context.getOperatorName())
                .remark(context.getRemark())
                .build();
    }

    private void persistResult(Long instructionId, FundGatewayResponse response) {
        if (response != null && response.isSuccess()) {
            fundInstructionRepo.updateExecutionResult(
                    instructionId,
                    FundInstruction.SUCCESS,
                    response.getActualAmount(),
                    response.getFundOrderId(),
                    response.getFundChannel(),
                    sanitizeJson(response.getRawResponse()));
        } else {
            fundInstructionRepo.updateExecutionResult(
                    instructionId,
                    FundInstruction.FAIL,
                    response == null ? null : response.getActualAmount(),
                    response == null ? null : response.getFundOrderId(),
                    response == null ? null : response.getFundChannel(),
                    response == null ? null : sanitizeJson(response.getRawResponse()));
        }
    }

    private String sanitizeJson(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("{")) {
            return trimmed;
        }
        String escaped = trimmed.replace("\"", "\\\"");
        return "{\"msg\":\"" + escaped + "\"}";
    }

    private String buildErrorMessage(Exception ex) {
        return ex == null ? "unknown error" : ex.getMessage();
    }
}
