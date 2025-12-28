package com.merchant.feecal.controller.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class RuleInfoResult implements Serializable {

    /**
     * 规则id
	 */
	private Long ruleId;

	/**
	 * 规则类型
	 */
	private String ruleType;

	/**
     * 如果是多城市，用英文逗号分隔
	 */
	private String cityCode;

	/**
	 * 如果是多城市，用英文逗号分隔
	 */
	private String cityName;

	/**
	 * 合同编码
	 */
	private String contractTempCode;

	/**
	 * 合同名称
	 */
	private String contractTempName;

	/**
	 * 合同版本
	 */
	private String contractTempVersion;

	/**
     * 规则的值
	 * json格式：[{"code":"公司税号", "name":"公司名称"}]
	 */
	private String ruleValue;
}
