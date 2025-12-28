package com.merchant.feecal.service.fund;

import com.merchant.common.exception.ServiceException;
import com.merchant.feecal.domain.assembler.FundInstructionAssembler;
import com.merchant.dao.tables.pojos.FeeCalFundInstructionEntity;
import com.merchant.feecal.dto.fund.FundInstructionCallbackRequest;
import com.merchant.feecal.dto.fund.FundInstructionDTO;
import com.merchant.feecal.dto.fund.FundInstructionOperateRequest;
import com.merchant.feecal.repo.FundInstructionRepo;
import com.merchant.feecal.service.FeeCalConstants.Status.FundInstruction;
import com.merchant.feecal.service.fund.model.FundExecutionContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 默认资金指令应用服务
 */
@Service
public class FundInstructionAppServiceImpl implements IFundInstructionAppService {

    @Resource
    private IFundInstructionGenerator fundInstructionGenerator;
    @Resource
    private FundInstructionRepo fundInstructionRepo;
    @Resource
    private IFundExecutor fundExecutor;
    @Resource
    private IFundBillingCallbackService fundBillingCallbackService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FundInstructionDTO> generateForBatch(String batchNo) {
        fundInstructionGenerator.generateForBatch(batchNo);
        return listByBatch(batchNo);
    }

    @Override
    public List<FundInstructionDTO> listByBatch(String batchNo) {
        return FundInstructionAssembler.toDTOs(fundInstructionRepo.queryByBatchNo(batchNo));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FundInstructionDTO execute(Long instructionId, FundInstructionOperateRequest request) {
        FeeCalFundInstructionEntity instruction = loadInstruction(instructionId);
        if (!FundInstruction.PENDING.equals(instruction.getFundStatus())) {
            throw new ServiceException("仅待执行的指令可以执行");
        }
        applyAttachments(instruction, request);
        FundExecutionContext context = buildContext(instructionId, request.getOperator(),
                request.getOperatorName(), request.getRemark());
        fundExecutor.execute(instruction, context);
        return refresh(instructionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FundInstructionDTO retry(Long instructionId, FundInstructionOperateRequest request) {
        FeeCalFundInstructionEntity instruction = loadInstruction(instructionId);
        if (!FundInstruction.FAIL.equals(instruction.getFundStatus())) {
            throw new ServiceException("仅失败指令可以重试");
        }
        applyAttachments(instruction, request);
        FundExecutionContext context = buildContext(instructionId, request.getOperator(),
                request.getOperatorName(), request.getRemark());
        fundExecutor.execute(instruction, context);
        return refresh(instructionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FundInstructionDTO callback(Long instructionId, FundInstructionCallbackRequest request) {
        FeeCalFundInstructionEntity instruction = loadInstruction(instructionId);
        FundExecutionContext context = buildContext(instructionId,
                request.getOperator(), request.getOperatorName(), request.getRemark());
        fundBillingCallbackService.callback(instruction, context);
        return refresh(instructionId);
    }

    private FeeCalFundInstructionEntity loadInstruction(Long instructionId) {
        return fundInstructionRepo.findById(instructionId)
                .orElseThrow(() -> new ServiceException("资金指令不存在: " + instructionId));
    }

    private FundInstructionDTO refresh(Long instructionId) {
        return FundInstructionAssembler.toDTO(loadInstruction(instructionId));
    }

    private FundExecutionContext buildContext(Long instructionId,
                                              String operator,
                                              String operatorName,
                                              String remark) {
        if (StringUtils.isBlank(operator)) {
            throw new ServiceException("operator 不能为空");
        }
        return FundExecutionContext.builder()
                .instructionId(instructionId)
                .operator(operator)
                .operatorName(operatorName)
                .remark(remark)
                .build();
    }

    private void applyAttachments(FeeCalFundInstructionEntity instruction, FundInstructionOperateRequest request) {
        if (instruction == null || request == null || CollectionUtils.isEmpty(request.getAttachmentIds())) {
            return;
        }
        String attachmentIds = request.getAttachmentIds()
                .stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.joining(","));
        fundInstructionRepo.updateAttachmentIds(instruction.getId(), attachmentIds);
        instruction.setAttachmentIds(attachmentIds);
    }
}
