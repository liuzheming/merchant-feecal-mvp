package com.merchant.feecal.service.summary;

import com.merchant.common.exception.ServiceException;
import com.merchant.dao.tables.pojos.FeeCalBatchEntity;
import com.merchant.dao.tables.pojos.FeeCalMerchantEntity;
import com.merchant.dao.tables.pojos.FeeCalTermDefEntity;
import com.merchant.dao.tables.pojos.FeeCalTermInstEntity;
import com.merchant.feecal.domain.assembler.SettlementAssembler;
import com.merchant.feecal.domain.model.SettlementBatch;
import com.merchant.feecal.domain.model.SettlementSummary;
import com.merchant.feecal.domain.model.TermItem;
import com.merchant.feecal.dto.FeeCalBillItemDTO;
import com.merchant.feecal.dto.FeeCalCalculateRequest;
import com.merchant.feecal.dto.FeeCalPageDTO;
import com.merchant.feecal.dto.FeeCalSaveRequest;
import com.merchant.feecal.dto.FeeCalSubmitRequest;
import com.merchant.feecal.service.TermCardInput;
import com.merchant.feecal.service.alloc.FeeCalTermInstAllocService;
import com.merchant.feecal.service.core.FeeCalCoreContext;
import com.merchant.feecal.service.core.IFeeCalCoreService;
import com.merchant.feecal.service.FeeCalConstants.Status.Batch;
import com.merchant.feecal.service.FeeCalConstants.Status.Term;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Description:
 * <p>
 *
 * @author lzm
 * @date 2025/11/24
 */
@Service
public class FeeCalSummaryService implements IFeeCalSummaryService {

	@Resource
	private IFeeCalCoreService feeCalCoreService;

	@Resource
	private FeeCalTermInstAllocService termInstAllocService;

	@Override
	public FeeCalPageDTO getPage(String batchNo) {
		FeeCalCoreContext coreContext = feeCalCoreService.loadCoreContext(batchNo);
		FeeCalBatchEntity batch = coreContext.getBatch();
		FeeCalMerchantEntity merchant = coreContext.getMerchant();

		feeCalCoreService.ensureBillingDataReady(coreContext);

		List<FeeCalTermInstEntity> termInsts = feeCalCoreService.queryTermInsts(batchNo, merchant.getId());
		Map<String, FeeCalTermDefEntity> termDefMap = feeCalCoreService.loadEnabledTermDefinitions();

		Map<Long, List<FeeCalBillItemDTO>> billItemsMap = termInstAllocService.loadBillItems(batchNo, termInsts);
		Map<String, FeeCalTermInstEntity> termInstMap = termInsts.stream()
			.collect(Collectors.toMap(FeeCalTermInstEntity::getTermCode, inst -> inst, (a, b) -> a));

		SettlementBatch settlementBatch = SettlementAssembler.toSettlementBatch(batch, merchant);
		List<TermItem> termItems = SettlementAssembler.toTermItems(termInsts, termDefMap);

		FeeCalPageDTO pageDTO = buildPageDTO(settlementBatch, termItems);
		attachBillAllocations(pageDTO, termInstMap, billItemsMap);
		return pageDTO;
	}

	@Override
	public FeeCalPageDTO calculate(FeeCalCalculateRequest request) {
		FeeCalCoreContext coreContext = feeCalCoreService.loadCoreContext(request.getBatchNo());
		FeeCalBatchEntity batch = coreContext.getBatch();
		FeeCalMerchantEntity merchant = coreContext.getMerchant();
		List<FeeCalTermInstEntity> termInsts = feeCalCoreService.queryTermInsts(request.getBatchNo(), merchant.getId());

		feeCalCoreService.ensureBillingDataReady(coreContext);

		Map<String, FeeCalTermDefEntity> termDefMap = feeCalCoreService.loadEnabledTermDefinitions();

		// 执行计算
		CalculateResult result = doCalculate(
			request.getDepositInfo().getDepositBalanceTotal(),
			request.getTermCards()
		);

		// 构建返回数据
		FeeCalPageDTO pageDTO = new FeeCalPageDTO();
		pageDTO.setBatchNo(request.getBatchNo());
		pageDTO.setBatchStatus(batch.getStatus());
		pageDTO.setBillingDataStatus(batch.getBillingDataStatus());

		// 主体信息
		FeeCalPageDTO.MerchantInfo merchantInfo = new FeeCalPageDTO.MerchantInfo();
		merchantInfo.setMerchantType(merchant.getMerchantType());
		merchantInfo.setMerchantCode(merchant.getMerchantCode());
		pageDTO.setMerchantInfo(merchantInfo);

		// 保证金信息（计算后的结果）
		FeeCalPageDTO.DepositInfo depositInfo = new FeeCalPageDTO.DepositInfo();
		depositInfo.setDepositBalanceTotal(request.getDepositInfo().getDepositBalanceTotal());
		depositInfo.setDepositBalanceRemain(result.getDepositBalanceRemain());
		depositInfo.setDepositAmountDeduct(result.getDepositAmountDeduct());
		depositInfo.setUnpaidAmountRemain(result.getUnpaidAmountRemain());
		pageDTO.setDepositInfo(depositInfo);

		// 费用项列表
		List<FeeCalPageDTO.TermCard> termCards = new ArrayList<>();
		for (FeeCalCalculateRequest.TermCard requestCard : request.getTermCards()) {
			FeeCalTermDefEntity termDef = termDefMap.get(requestCard.getCode());
			if (termDef == null) {
				continue;
			}
			boolean autoLoad = termDef.getAutoLoad() != null && termDef.getAutoLoad() == 1;

			FeeCalPageDTO.TermCard termCard = new FeeCalPageDTO.TermCard();
			termCard.setCode(requestCard.getCode());
			termCard.setName(termDef.getName());
			termCard.setStatus(Term.DRAFT);
			termCard.setAutoLoad(autoLoad);
			termCard.setUnpaidAmount(requestCard.getUnpaidAmount());
			termCard.setDeductFlag(requestCard.getDeductFlag());
			termCard.setDeductAmount(requestCard.getDeductAmount());
			termCards.add(termCard);
		}
		pageDTO.setTermCards(termCards);

		Map<String, FeeCalTermInstEntity> termInstMap = termInsts.stream()
			.collect(Collectors.toMap(FeeCalTermInstEntity::getTermCode, inst -> inst, (a, b) -> a));
		Map<Long, List<FeeCalBillItemDTO>> billItemsMap = termInstAllocService.loadBillItems(batch.getBatchNo(), termInsts);
		attachBillAllocations(pageDTO, termInstMap, billItemsMap);
		return pageDTO;
	}

	private FeeCalPageDTO buildPageDTO(SettlementBatch settlementBatch, List<TermItem> termItems) {
		FeeCalPageDTO pageDTO = new FeeCalPageDTO();
		if (settlementBatch != null) {
			pageDTO.setBatchNo(settlementBatch.getBatchNo());
			pageDTO.setBatchStatus(settlementBatch.getStatus());
			pageDTO.setBillingDataStatus(settlementBatch.getBillingDataStatus());

			FeeCalPageDTO.MerchantInfo merchantInfo = new FeeCalPageDTO.MerchantInfo();
			merchantInfo.setMerchantType(settlementBatch.getMerchantType());
			merchantInfo.setMerchantCode(settlementBatch.getMerchantCode());
			pageDTO.setMerchantInfo(merchantInfo);

			SettlementSummary summary = settlementBatch.getSummary();
			FeeCalPageDTO.DepositInfo depositInfo = new FeeCalPageDTO.DepositInfo();
			if (summary != null) {
				depositInfo.setDepositBalanceTotal(summary.getDepositBalanceTotal());
				depositInfo.setDepositBalanceRemain(summary.getDepositBalanceRemain());
				depositInfo.setDepositAmountDeduct(summary.getDepositAmountDeduct());
				depositInfo.setUnpaidAmountRemain(summary.getUnpaidAmountRemain());
			}
			pageDTO.setDepositInfo(depositInfo);
		}

		List<FeeCalPageDTO.TermCard> termCardDTOs = new ArrayList<>();
		if (termItems != null) {
			for (TermItem termItem : termItems) {
				FeeCalPageDTO.TermCard termCard = new FeeCalPageDTO.TermCard();
				termCard.setCode(termItem.getCode());
				termCard.setName(termItem.getName());
				termCard.setStatus(termItem.getStatus());
					termCard.setAutoLoad(termItem.getAutoLoad());
				termCard.setUnpaidAmount(termItem.getUnpaidAmount());
				termCard.setDeductFlag(termItem.getDeductFlag());
				termCard.setDeductAmount(termItem.getDeductAmount());
				termCardDTOs.add(termCard);
			}
		}
		pageDTO.setTermCards(termCardDTOs);
		return pageDTO;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public FeeCalPageDTO saveDraft(FeeCalSaveRequest request) {
		FeeCalCoreContext coreContext = feeCalCoreService.loadCoreContext(request.getBatchNo());
		FeeCalBatchEntity batch = coreContext.getBatch();
		FeeCalMerchantEntity merchant = coreContext.getMerchant();

		if (!Batch.EDITING.equals(batch.getStatus())) {
			throw new ServiceException("批次状态不允许保存: " + batch.getStatus());
		}

		feeCalCoreService.ensureBillingDataReady(coreContext);

		// 执行计算
		CalculateResult result = doCalculate(
			request.getDepositInfo().getDepositBalanceTotal(),
			request.getTermCards()
		);

		// 更新批次
		batch.setDepositBalanceTotal(request.getDepositInfo().getDepositBalanceTotal());
		batch.setDepositAmountDeduct(result.getDepositAmountDeduct());
		batch.setDepositBalanceRemain(result.getDepositBalanceRemain());
		batch.setUnpaidAmountRemain(result.getUnpaidAmountRemain());
		feeCalCoreService.updateBatch(batch);

		List<FeeCalTermInstEntity> termInsts = feeCalCoreService.queryTermInsts(request.getBatchNo(), merchant.getId());
		Map<String, FeeCalTermInstEntity> termInstMap = termInsts.stream()
			.collect(Collectors.toMap(FeeCalTermInstEntity::getTermCode, inst -> inst));

		for (FeeCalSaveRequest.TermCard requestCard : request.getTermCards()) {
			FeeCalTermInstEntity termInst = termInstMap.get(requestCard.getCode());
			if (termInst != null) {
				termInst.setUnpaidAmount(requestCard.getUnpaidAmount());
				termInst.setDeductFlag(requestCard.getDeductFlag() != null && requestCard.getDeductFlag() ? (byte) 1 : (byte) 0);
				termInst.setDeductAmount(requestCard.getDeductAmount() != null ? requestCard.getDeductAmount() : BigDecimal.ZERO);
				termInst.setStatus(Term.DRAFT);
				feeCalCoreService.updateTermInst(termInst);
				if (termInst.getBillingAllocEnabled() != null && termInst.getBillingAllocEnabled() == 1) {
					List<FeeCalBillItemDTO> billItems = requestCard.getBillItems();
					if (billItems == null) {
						throw new ServiceException("请提供账单分配明细，termCode=" + termInst.getTermCode());
					}
					termInstAllocService.applyAllocations(termInst, billItems, termInst.getDeductAmount());
				}
			}
		}

		// 返回页面数据
		return getPage(request.getBatchNo());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public FeeCalPageDTO submit(FeeCalSubmitRequest request) {
		FeeCalCoreContext coreContext = feeCalCoreService.loadCoreContext(request.getBatchNo());
		FeeCalBatchEntity batch = coreContext.getBatch();
		FeeCalMerchantEntity merchant = coreContext.getMerchant();

		if (!Batch.EDITING.equals(batch.getStatus())) {
			throw new ServiceException("批次状态不允许提交: " + batch.getStatus());
		}

		feeCalCoreService.ensureBillingDataReady(coreContext);

		// 再次校验数据
		CalculateResult result = doCalculate(
			request.getDepositInfo().getDepositBalanceTotal(),
			request.getTermCards()
		);

		// 更新批次状态为 DONE
		batch.setDepositBalanceTotal(request.getDepositInfo().getDepositBalanceTotal());
		batch.setDepositAmountDeduct(result.getDepositAmountDeduct());
		batch.setDepositBalanceRemain(result.getDepositBalanceRemain());
		batch.setUnpaidAmountRemain(result.getUnpaidAmountRemain());
		batch.setStatus(Batch.DONE);
		feeCalCoreService.updateBatch(batch);

		feeCalCoreService.batchUpdateTermStatus(request.getBatchNo(), Term.DONE);

		List<FeeCalTermInstEntity> termInsts = feeCalCoreService.queryTermInsts(request.getBatchNo(), merchant.getId());
		Map<String, FeeCalTermInstEntity> termInstMap = termInsts.stream()
			.collect(Collectors.toMap(FeeCalTermInstEntity::getTermCode, inst -> inst));

		for (FeeCalSubmitRequest.TermCard requestCard : request.getTermCards()) {
			FeeCalTermInstEntity termInst = termInstMap.get(requestCard.getCode());
			if (termInst != null) {
				termInst.setUnpaidAmount(requestCard.getUnpaidAmount());
				termInst.setDeductFlag(requestCard.getDeductFlag() != null && requestCard.getDeductFlag() ? (byte) 1 : (byte) 0);
				termInst.setDeductAmount(requestCard.getDeductAmount() != null ? requestCard.getDeductAmount() : BigDecimal.ZERO);
				termInst.setStatus(Term.DONE);
				feeCalCoreService.updateTermInst(termInst);
				if (termInst.getBillingAllocEnabled() != null && termInst.getBillingAllocEnabled() == 1) {
					List<FeeCalBillItemDTO> billItems = requestCard.getBillItems();
					if (billItems == null) {
						throw new ServiceException("请提供账单分配明细，termCode=" + termInst.getTermCode());
					}
					termInstAllocService.applyAllocations(termInst, billItems, termInst.getDeductAmount());
				}
			}
		}

		// 返回页面数据
		FeeCalPageDTO pageDTO = getPage(request.getBatchNo());
		pageDTO.setBatchStatus(Batch.DONE);
		pageDTO.setBillingDataStatus(batch.getBillingDataStatus());
		// 更新费用项状态为 DONE，不允许编辑
		if (CollectionUtils.isNotEmpty(pageDTO.getTermCards())) {
			for (FeeCalPageDTO.TermCard termCard : pageDTO.getTermCards()) {
				termCard.setStatus(Term.DONE);
			}
		}

		return pageDTO;
	}

	/**
	 * 执行计算逻辑
	 */
	private CalculateResult doCalculate(BigDecimal depositBalanceTotal, List<? extends TermCardInput> termCards) {
		BigDecimal safeDepositTotal = depositBalanceTotal == null ? BigDecimal.ZERO : depositBalanceTotal;
		BigDecimal totalUnpaidAmount = BigDecimal.ZERO;
		BigDecimal depositAmountDeduct = BigDecimal.ZERO;

		if (termCards != null) {
			for (TermCardInput termCard : termCards) {
				if (termCard == null) {
					continue;
				}

				// 欠费金额、抵扣flag，都不允许为 null
				if (termCard.getUnpaidAmount() == null) {
					throw new ServiceException("欠费金额不能为空，费用项：" + termCard.getCode());
				}
				if (termCard.getDeductFlag() == null) {
					throw new ServiceException("是否抵扣不能为空，费用项：" + termCard.getCode());
				}
//				if (termCard.getDeductFlag() && BigDecimal.ZERO.equals(termCard.getDeductAmount())) {
//					throw new ServiceException("抵扣金额不能为0，费用项：" + termCard.getCode());
//				}
				// 抵扣金额不能大于欠费金额
				if (termCard.getDeductFlag() && termCard.getDeductAmount().compareTo(termCard.getUnpaidAmount()) > 0) {
					throw new ServiceException("抵扣金额不能大于欠费金额，费用项：" + termCard.getCode());
				}

				BigDecimal unpaid = termCard.getUnpaidAmount() == null ? BigDecimal.ZERO : termCard.getUnpaidAmount();
				totalUnpaidAmount = totalUnpaidAmount.add(unpaid);

				if (Boolean.TRUE.equals(termCard.getDeductFlag())) {
					BigDecimal deduct = termCard.getDeductAmount() == null ? BigDecimal.ZERO : termCard.getDeductAmount();
					if (deduct.compareTo(unpaid) > 0) {
						deduct = unpaid;
					}
					depositAmountDeduct = depositAmountDeduct.add(deduct);
				}
			}
		}

		BigDecimal depositBalanceRemain = safeDepositTotal.subtract(depositAmountDeduct);
		if (depositBalanceRemain.compareTo(BigDecimal.ZERO) < 0) {
			throw new ServiceException("保证金余额不足，无法完成抵扣");
		}

		BigDecimal unpaidAmountRemain = totalUnpaidAmount.subtract(depositAmountDeduct);
		if (unpaidAmountRemain.compareTo(BigDecimal.ZERO) < 0) {
			unpaidAmountRemain = BigDecimal.ZERO;
		}

		CalculateResult result = new CalculateResult();
		result.setDepositAmountDeduct(depositAmountDeduct.setScale(2, RoundingMode.HALF_UP));
		result.setDepositBalanceRemain(depositBalanceRemain.setScale(2, RoundingMode.HALF_UP));
		result.setUnpaidAmountRemain(unpaidAmountRemain.setScale(2, RoundingMode.HALF_UP));
		return result;
	}

	/**
	 * 计算结果内部类
	 */
	private static class CalculateResult {
		private BigDecimal depositAmountDeduct;
		private BigDecimal depositBalanceRemain;
		private BigDecimal unpaidAmountRemain;

		public BigDecimal getDepositAmountDeduct() {
			return depositAmountDeduct;
		}

		public void setDepositAmountDeduct(BigDecimal depositAmountDeduct) {
			this.depositAmountDeduct = depositAmountDeduct;
		}

		public BigDecimal getDepositBalanceRemain() {
			return depositBalanceRemain;
		}

		public void setDepositBalanceRemain(BigDecimal depositBalanceRemain) {
			this.depositBalanceRemain = depositBalanceRemain;
		}

		public BigDecimal getUnpaidAmountRemain() {
			return unpaidAmountRemain;
		}

		public void setUnpaidAmountRemain(BigDecimal unpaidAmountRemain) {
			this.unpaidAmountRemain = unpaidAmountRemain;
		}
	}

	private void attachBillAllocations(FeeCalPageDTO pageDTO,
									   Map<String, FeeCalTermInstEntity> termInstMap,
									   Map<Long, List<FeeCalBillItemDTO>> billItemsMap) {
		if (pageDTO == null || CollectionUtils.isEmpty(pageDTO.getTermCards())) {
			return;
		}
		for (FeeCalPageDTO.TermCard termCard : pageDTO.getTermCards()) {
			if (termCard == null) {
				continue;
			}
			FeeCalTermInstEntity termInst = termInstMap.get(termCard.getCode());
			if (termInst == null) {
				termCard.setBillItems(Collections.emptyList());
				continue;
			}
			boolean support = termInst.getBillingAllocEnabled() != null && termInst.getBillingAllocEnabled() == 1;
			termCard.setSupportBillAlloc(support);
			if (!support) {
				termCard.setBillItems(Collections.emptyList());
				continue;
			}

			List<FeeCalBillItemDTO> billItems = billItemsMap.get(termInst.getId());
			termCard.setBillItems(billItems == null ? Collections.emptyList() : billItems);
		}
	}
}
