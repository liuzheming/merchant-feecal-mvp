package com.merchant.feecal.service.core;

import com.merchant.dao.tables.pojos.FeeCalBatchEntity;
import com.merchant.dao.tables.pojos.FeeCalMerchantEntity;

/**
 * 核心上下文
 */
public class FeeCalCoreContext {
	private final FeeCalBatchEntity batch;
	private final FeeCalMerchantEntity merchant;

	public FeeCalCoreContext(FeeCalBatchEntity batch, FeeCalMerchantEntity merchant) {
		this.batch = batch;
		this.merchant = merchant;
	}

	public FeeCalBatchEntity getBatch() {
		return batch;
	}

	public FeeCalMerchantEntity getMerchant() {
		return merchant;
	}
}
