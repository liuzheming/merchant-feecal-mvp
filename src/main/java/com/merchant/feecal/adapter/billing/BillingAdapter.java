package com.merchant.feecal.adapter.billing;

import com.merchant.dao.tables.pojos.FeeCalBillingSnapshotEntity;

import java.util.Collections;
import java.util.List;

/**
 * 上游计费适配层入口
 */
public interface BillingAdapter {

	/**
	 * 拉取上游账单并转换为清算账单快照
	 */
	List<FeeCalBillingSnapshotEntity> pullBillingViews(BillingPullContext context);

	/**
	 * 资金执行完成后，通知上游计费系统更新账单状态
	 */
	void pushFundResult(BillingCallbackContext context);

	/**
	 * 默认空结果
	 */
	default List<FeeCalBillingSnapshotEntity> emptyResult() {
		return Collections.emptyList();
	}

	/**
	 * 拉账上下文
	 */
	class BillingPullContext {
		private final String batchNo;
		private final String merchantType;
		private final String merchantCode;

		public BillingPullContext(String batchNo, String merchantType, String merchantCode) {
			this.batchNo = batchNo;
			this.merchantType = merchantType;
			this.merchantCode = merchantCode;
		}

		public String getBatchNo() {
			return batchNo;
		}

		public String getMerchantType() {
			return merchantType;
		}

		public String getMerchantCode() {
			return merchantCode;
		}
	}

	/**
	 * 回调上游计费的上下文
	 */
	class BillingCallbackContext {
		private String batchNo;
		private Long termInstId;
		private String termCode;
		private Long fundInstructionId;
		private String fundBizType;
		private String operator;
		private String operatorName;
		private String remark;
		private java.math.BigDecimal instructionShouldAmount;
		private java.math.BigDecimal instructionActualAmount;
		private java.util.List<BillingCallbackItem> items;

		public String getBatchNo() {
			return batchNo;
		}

		public void setBatchNo(String batchNo) {
			this.batchNo = batchNo;
		}

		public Long getTermInstId() {
			return termInstId;
		}

		public void setTermInstId(Long termInstId) {
			this.termInstId = termInstId;
		}

		public String getTermCode() {
			return termCode;
		}

		public void setTermCode(String termCode) {
			this.termCode = termCode;
		}

		public Long getFundInstructionId() {
			return fundInstructionId;
		}

		public void setFundInstructionId(Long fundInstructionId) {
			this.fundInstructionId = fundInstructionId;
		}

		public String getFundBizType() {
			return fundBizType;
		}

		public void setFundBizType(String fundBizType) {
			this.fundBizType = fundBizType;
		}

		public String getOperator() {
			return operator;
		}

		public void setOperator(String operator) {
			this.operator = operator;
		}

		public String getOperatorName() {
			return operatorName;
		}

		public void setOperatorName(String operatorName) {
			this.operatorName = operatorName;
		}

		public String getRemark() {
			return remark;
		}

		public void setRemark(String remark) {
			this.remark = remark;
		}

		public java.math.BigDecimal getInstructionShouldAmount() {
			return instructionShouldAmount;
		}

		public void setInstructionShouldAmount(java.math.BigDecimal instructionShouldAmount) {
			this.instructionShouldAmount = instructionShouldAmount;
		}

		public java.math.BigDecimal getInstructionActualAmount() {
			return instructionActualAmount;
		}

		public void setInstructionActualAmount(java.math.BigDecimal instructionActualAmount) {
			this.instructionActualAmount = instructionActualAmount;
		}

		public java.util.List<BillingCallbackItem> getItems() {
			return items;
		}

		public void setItems(java.util.List<BillingCallbackItem> items) {
			this.items = items;
		}
	}

	/**
	 * 单条账单回调明细
	 */
	class BillingCallbackItem {
		private Long billingSnapshotId;
		private String billingOrderId;
		private java.math.BigDecimal allocAmount;

		public Long getBillingSnapshotId() {
			return billingSnapshotId;
		}

		public void setBillingSnapshotId(Long billingSnapshotId) {
			this.billingSnapshotId = billingSnapshotId;
		}

		public String getBillingOrderId() {
			return billingOrderId;
		}

		public void setBillingOrderId(String billingOrderId) {
			this.billingOrderId = billingOrderId;
		}

		public java.math.BigDecimal getAllocAmount() {
			return allocAmount;
		}

		public void setAllocAmount(java.math.BigDecimal allocAmount) {
			this.allocAmount = allocAmount;
		}
	}
}
