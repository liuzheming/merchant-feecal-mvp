package com.merchant.feecal.termdef.impl;

import com.merchant.dao.tables.pojos.FeeCalTermDefEntity;
import com.merchant.feecal.termdef.TermDefService;
import com.merchant.feecal.repo.FeeCalTermDefRepo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 默认费用项定义服务实现：直接读取数据库启用项
 */
@Service
public class DefaultTermDefService implements TermDefService {

    @Resource
    private FeeCalTermDefRepo feeCalTermDefRepo;

    @Override
    public List<FeeCalTermDefEntity> loadEnabledTerms() {
        return feeCalTermDefRepo.queryAllEnabled();
    }
}
