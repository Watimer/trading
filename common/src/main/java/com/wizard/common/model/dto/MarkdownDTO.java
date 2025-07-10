package com.wizard.common.model.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wizard
 * @date 2025年07月10日 15:45
 * @desc
 */
@Data
@Builder
public class MarkdownDTO implements Serializable {

	/**
	 * 首屏会话透出的展示内容
	 */
	private String title;

	/**
	 * markdown格式的消息
	 */
	private String text;
}
