package com.wizard.common.model.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wizard
 * @date 2025年07月12日 09:32
 * @desc
 */
@Data
@Builder
public class NewsTokenVO implements Serializable {

	/**
	 * 币种
	 */
	private String symbol;

	/**
	 * 方向-BUY OR SELL
	 */
	private String side;

	/**
	 * 交易所
	 */
	private String exchange;
}
