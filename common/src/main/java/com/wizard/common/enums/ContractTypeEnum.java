package com.wizard.common.enums;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author wizard
 * @date 2024-10-10
 * @desc 合约类型枚举
 */
@Getter
@NoArgsConstructor
public enum ContractTypeEnum {

	PERPETUAL("PERPETUAL","永续合约"),
	CURRENT_MONTH("CURRENT_MONTH","当月交割合约"),
	CURRENT_QUARTER("CURRENT_QUARTER","当季交割合约"),
	NEXT_QUARTER("NEXT_QUARTER","次季交割合约"),
	PERPETUAL_DELIVERING("PERPETUAL_DELIVERING","交割结算中合约"),
	NEXT_MONTH("NEXT_MONTH","次月交割合约");

	private String code;

	private String name;


	ContractTypeEnum(String code,String name){
		this.code = code;
		this.name = name;
	}

	/**
	 * 根据 code 查找对应的枚举对象
	 */
	public static ContractTypeEnum fromCode(String code) {
		for (ContractTypeEnum status : ContractTypeEnum.values()) {
			if (code.equals(status.getCode())) {
				return status;
			}
		}
		// 如果没有找到，抛出异常或者返回 null
		throw new IllegalArgumentException("Unknown code: " + code);
	}
}
