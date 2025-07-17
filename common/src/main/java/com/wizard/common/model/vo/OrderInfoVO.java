package com.wizard.common.model.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author wizard
 * @date 2025年07月17日 16:04
 * @desc
 */
@Data
@Builder
public class OrderInfoVO implements Serializable {

	private Long orderId;

	private String symbol;

	private String status;

	private String clientOrderId;

	private String price;

	/**
	 * 均价
	 */
	private BigDecimal avgPrice;

	private BigDecimal origQty;

	private BigDecimal executedQty;

	private BigDecimal cumQuote;

	private String timeInForce;

	private String type;

	private String reduceOnly;

	private String closePosition;

	private String side;

	private String positionSide;

	private String stopPrice;

	private String workingType;

	private String priceMatch;

	private String selfTradePreventionMode;

	private String goodTillDate;

	private String priceProtect;

	private String origType;

	private Long time;

	private Long updateTime;
}
