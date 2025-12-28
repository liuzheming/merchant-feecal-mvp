package com.merchant.feecal.service.summary;

import com.merchant.feecal.dto.FeeCalCalculateRequest;
import com.merchant.feecal.dto.FeeCalPageDTO;
import com.merchant.feecal.dto.FeeCalSaveRequest;
import com.merchant.feecal.dto.FeeCalSubmitRequest;

/**
 * Description:
 * <p>
 *
 * @author lzm
 * @date 2025/11/24
 */
public interface IFeeCalSummaryService {

	/**
	 * 渲染清算页面（根据 batchNo 查询）
	 */
	FeeCalPageDTO getPage(String batchNo);

	/**
	 * 实时计算接口（不落库）
	 */
	FeeCalPageDTO calculate(FeeCalCalculateRequest request);

	/**
	 * 保存草稿（落库）
	 */
	FeeCalPageDTO saveDraft(FeeCalSaveRequest request);

	/**
	 * 提交清算（落库 + DONE）
	 */
	FeeCalPageDTO submit(FeeCalSubmitRequest request);


}
