package com.merchant.feecal.dto;

import com.merchant.feecal.dto.fund.FundInstructionDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class FeeCalAutoStartResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String batchNo;
    private List<FundInstructionDTO> fundInstructions;
}
