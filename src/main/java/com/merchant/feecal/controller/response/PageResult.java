package com.merchant.feecal.controller.response;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * @author : kaerKing
 * @date : 2023/10/19
 */
@Data
public class PageResult<T> {
	private Integer pageNo;
	private Integer pageSize;
	private Integer total;
	private List<T> list;

	public static <T> PageResult<T> of(int pageNo, int pageSize, int total, List<T> data) {
		PageResult<T> pageResult = new PageResult<>();
		pageResult.setPageNo(pageNo);
		pageResult.setPageSize(pageSize);
		pageResult.setTotal(total);
		pageResult.setList(data);
		return pageResult;
	}


	public static <T> PageResult<T> empty(int pageNo, int pageSize) {
		return of(pageNo, pageSize, 0, Lists.newArrayList());
	}
}
