package com.wizard.business.service;

import com.alibaba.fastjson.JSONObject;
import com.binance.connector.futures.client.impl.um_futures.UMMarket;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;

/**
 * @author wizard
 * @date 2025年07月11日 21:59
 * @desc
 */
@Slf4j
@Service
public class UMMarketService {

	@Resource
	@Qualifier("binanceUMMarket")
	UMMarket binanceUMMarket;

	/**
	 * 查询指定标的最新价格
	 * @param symbol
	 * @return
	 */
	public BigDecimal getMarketPrice(String symbol) {
		LinkedHashMap<String, Object> parametersPrice = new LinkedHashMap<>();
		parametersPrice.put("symbol", symbol);
		// 查看当前价格
		String string = binanceUMMarket.tickerSymbol(parametersPrice);
		JSONObject jsonObject = JSONObject.parseObject(string);
		JSONObject data = jsonObject.getJSONObject("data");
		return data.getBigDecimal("price");
	}
}
