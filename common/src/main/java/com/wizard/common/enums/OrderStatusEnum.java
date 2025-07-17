package com.wizard.common.enums;

import lombok.Getter;

/**
 * @author wizard
 * @date 2025年07月17日 16:26
 * @desc 订单状态
 */
@Getter
public enum OrderStatusEnum {
	NEW("NEW"),
	PARTIALLY_FILLED("PARTIALLY_FILLED"),
	FILLED("FILLED"),
	CANCELED("CANCELED"),
	REJECTED("REJECTED"),
	EXPIRED("EXPIRED"),
	EXPIRED_IN_MATCH("EXPIRED_IN_MATCH");

	private final String code;

	OrderStatusEnum(String code) {
		this.code = code;
	}
}
