package com.merchant.feecal.service.core;

import com.merchant.common.exception.ServiceException;
import com.merchant.dao.tables.pojos.FeeCalBatchEntity;
import com.merchant.dao.tables.pojos.FeeCalFundInstructionEntity;
import com.merchant.dao.tables.pojos.FeeCalTermDefEntity;
import com.merchant.dao.tables.pojos.FeeCalTermInstEntity;
import com.merchant.feecal.dto.FeeCalStartRequest;
import com.merchant.feecal.dto.FeeCalStartResponse;
import com.merchant.feecal.service.fund.model.FundExecutionContext;

import java.util.List;
import java.util.Map;

/**
 * 费用清算核心服务
 */
public interface IFeeCalCoreService {

    /**
     * 发起清算（创建批次）
     */
    FeeCalStartResponse start(FeeCalStartRequest request);

    /**
     * 加载批次及主体上下文，校验存在性
     */
    FeeCalCoreContext loadCoreContext(String batchNo) throws ServiceException;

    /**
     * 查询费用项定义
     */
    Map<String, FeeCalTermDefEntity> loadEnabledTermDefinitions();

    /**
     * 查询费用项实例
     */
    List<FeeCalTermInstEntity> queryTermInsts(String batchNo, Long merchantId);

    /**
     * 更新批次
     */
    void updateBatch(FeeCalBatchEntity batch);

    /**
     * 批量更新费用项状态
     */
    void batchUpdateTermStatus(String batchNo, String status);

    /**
     * 更新费用项实例
     */
    void updateTermInst(FeeCalTermInstEntity termInst);

    /**
     * 资金指令成功后，驱动计费回写
     */
    void onFundInstructionSuccess(FeeCalFundInstructionEntity instruction, FundExecutionContext context);

	void ensureBillingDataReady(FeeCalCoreContext coreContext);
}
