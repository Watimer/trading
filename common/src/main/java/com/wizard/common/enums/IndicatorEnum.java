package com.wizard.common.enums;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author wizard
 * @date 2025年07月10日 11:24
 * @desc
 */
@Getter
@NoArgsConstructor
public enum IndicatorEnum {

	MA("MA"),
	EMA("EMA"),
	MACD("MACD"),
	BOLL("BOLL"),
	SUPER_TREND("SUPER_TREND");

	private String code;

	IndicatorEnum(String code) {
		this.code = code;
	}

	public static IndicatorEnum fromCode(String code) {
		for (IndicatorEnum status : IndicatorEnum.values()) {
			if (code.equals(status.getCode())) {
				return status;
			}
		}
		// 如果没有找到，抛出异常或者返回 null
		throw new IllegalArgumentException("Unknown code: " + code);
	}

}
