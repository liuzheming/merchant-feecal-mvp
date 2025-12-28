package com.merchant.feecal.domain.assembler;

import com.merchant.dao.tables.pojos.FeeCalFundInstructionEntity;
import com.merchant.feecal.dto.fund.FundInstructionDTO;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FundInstruction 转换器
 */
public class FundInstructionAssembler {

    private FundInstructionAssembler() {
    }

    public static FundInstructionDTO toDTO(FeeCalFundInstructionEntity entity) {
        if (entity == null) {
            return null;
        }
        FundInstructionDTO dto = new FundInstructionDTO();
        dto.setId(entity.getId());
        dto.setBatchNo(entity.getBatchNo());
        dto.setTermInstId(entity.getTermInstId());
        dto.setSettleSubjectType(entity.getSettleSubjectType());
        dto.setSettleSubjectNo(entity.getSettleSubjectNo());
        dto.setPayerType(entity.getPayerType());
        dto.setPayerNo(entity.getPayerNo());
        dto.setPayeeType(entity.getPayeeType());
        dto.setPayeeNo(entity.getPayeeNo());
        dto.setFundDirection(entity.getFundDirection());
        dto.setFundBizType(entity.getFundBizType());
        dto.setAccountType(entity.getAccountType());
        dto.setShouldAmount(entity.getShouldAmount());
        dto.setActualAmount(entity.getActualAmount());
        dto.setFundStatus(entity.getFundStatus());
        dto.setCallbackStatus(entity.getCallbackStatus());
        dto.setFundOrderId(entity.getFundOrderId());
        dto.setFundChannel(entity.getFundChannel());
        dto.setFundOrderInfo(entity.getFundOrderInfo() == null ? null : entity.getFundOrderInfo().data());
        dto.setAttachmentIds(entity.getAttachmentIds());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public static List<FundInstructionDTO> toDTOs(List<FeeCalFundInstructionEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(FundInstructionAssembler::toDTO)
                .collect(Collectors.toList());
    }
}
