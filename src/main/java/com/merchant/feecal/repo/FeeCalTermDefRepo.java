package com.merchant.feecal.repo;

import com.merchant.dao.tables.pojos.FeeCalTermDefEntity;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.merchant.dao.Tables.FEE_CAL_TERM_DEF;

/**
 * 费用项定义表 Repository
 */
@Component
public class FeeCalTermDefRepo {

    @Resource
    private DSLContext dslContext;

    /**
     * 查询所有启用的费用项定义
     */
    public List<FeeCalTermDefEntity> queryAllEnabled() {
        return dslContext.selectFrom(FEE_CAL_TERM_DEF)
                .where(FEE_CAL_TERM_DEF.ENABLED.eq((byte) 1))
                .orderBy(FEE_CAL_TERM_DEF.SORT_NO.asc())
                .fetchInto(FeeCalTermDefEntity.class);
    }

    /**
     * 根据 code 查询
     */
    public FeeCalTermDefEntity queryByCode(String code) {
        return dslContext.selectFrom(FEE_CAL_TERM_DEF)
                .where(FEE_CAL_TERM_DEF.CODE.eq(code))
                .fetchOneInto(FeeCalTermDefEntity.class);
    }

    /**
     * 查询支持账单分配的费用项 code
     */
    public List<String> listBillAllocEnabledCodes() {
        return dslContext.select(FEE_CAL_TERM_DEF.CODE)
                .from(FEE_CAL_TERM_DEF)
                .where(FEE_CAL_TERM_DEF.BILL_ALLOC_ENABLED.eq(1))
                .and(FEE_CAL_TERM_DEF.ENABLED.eq((byte) 1))
                .fetchInto(String.class);
    }
}
