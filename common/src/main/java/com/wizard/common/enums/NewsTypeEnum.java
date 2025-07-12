package com.wizard.common.enums;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author wizard
 * @date 2025年07月12日 09:50
 * @desc 新闻类型枚举
 */
@Getter
public enum NewsTypeEnum {

	LISTING("上架"),
	DELISTING("下架"),
	UNKNOWN("未知");

	private final String description;

	NewsTypeEnum(String description) {
		this.description = description;
	}
}
