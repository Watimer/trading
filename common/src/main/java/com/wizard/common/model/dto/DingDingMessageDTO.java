package com.wizard.common.model.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wizard
 * @date 2025年07月10日 15:44
 * @desc
 */
@Data
@Builder
public class DingDingMessageDTO implements Serializable {

	private String msgtype;

	private MarkdownDTO markdown;

	private String context;
}
