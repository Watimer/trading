package com.wizard.common.model.dto;

import com.wizard.common.enums.ContractTypeEnum;
import com.wizard.common.enums.IntervalEnum;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wizard
 * @date 2024-10-10
 * @desc
 */
@Data
@Builder
public class SymbolLineDTO implements Serializable {

	/**
	 * 标的
	 */
	private String symbol;

	/**
	 * 时间级别
	 */
	private String interval;

	/**
	 * 类型
	 */
	private String contractType;

	/**
	 * 记录数
	 */
	private Integer limit;
}
