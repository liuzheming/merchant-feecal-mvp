package com.merchant.feecal.termdef;

import com.merchant.dao.tables.pojos.FeeCalTermDefEntity;

import java.util.List;

/**
 * 费用项定义服务（M5 TermDefinition 层入口）
 */
public interface TermDefService {

    /**
     * 加载当前启用的费用项定义
     */
    List<FeeCalTermDefEntity> loadEnabledTerms();
}
