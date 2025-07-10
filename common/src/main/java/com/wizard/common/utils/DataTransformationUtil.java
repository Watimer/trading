package com.wizard.common.utils;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.wizard.common.enums.IntervalEnum;
import com.wizard.common.model.MarketQuotation;
import com.wizard.common.model.dto.SymbolLineDTO;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wizard
 * @date 2024-10-10
 * @desc 数据转化工具
 */
public class DataTransformationUtil {

	public static List<MarketQuotation> transform(SymbolLineDTO symbolLineDTO, String dataString) {
		JSONArray jsonArray = JSONArray.parseArray(dataString);
		// 行情数据
		List<MarketQuotation> listMarketQuotation = (List<MarketQuotation>) jsonArray.parallelStream()
				.map(item -> {
					JSONArray jsonItem = JSONArray.parseArray(item.toString());
					return getMarketQuotation(symbolLineDTO, jsonItem);
				}).collect(Collectors.toList());

		return listMarketQuotation;
	}

	private static MarketQuotation getMarketQuotation(SymbolLineDTO symbolLineDTO,JSONArray jsonItem){
		MarketQuotation marketQuotation = new MarketQuotation();
		Long openTime = jsonItem.getLong(0);
		Long closeTime = jsonItem.getLong(6);
		// 转化时间,并设置时区为东八区
		marketQuotation.setCloseTime(LocalDateTimeUtil.of(closeTime, ZoneId.of(ZoneId.SHORT_IDS.get("CTT"))));
		marketQuotation.setTimestamp(LocalDateTimeUtil.of(openTime, ZoneId.of(ZoneId.SHORT_IDS.get("CTT"))));

		Double openPrice = jsonItem.getDouble(1);
		marketQuotation.setOpen(openPrice);
		marketQuotation.setBigDecimalOpen(jsonItem.getBigDecimal(1));

		Double highPrice = jsonItem.getDouble(2);
		marketQuotation.setHigh(highPrice);
		marketQuotation.setBigDecimalHigh(jsonItem.getBigDecimal(2));

		Double lowPrice = jsonItem.getDouble(3);
		marketQuotation.setLow(lowPrice);
		marketQuotation.setBigDecimalLow(jsonItem.getBigDecimal(3));

		Double closePrice = jsonItem.getDouble(4);
		marketQuotation.setClose(closePrice);
		marketQuotation.setBigDecimalClose(jsonItem.getBigDecimal(4));

		Double volume = jsonItem.getDouble(5);
		marketQuotation.setVolume(volume);
		marketQuotation.setBigDecimalVolume(jsonItem.getBigDecimal(5));

		Double amount = jsonItem.getDouble(7);
		marketQuotation.setAmount(amount);
		marketQuotation.setBigDecimalAmount(jsonItem.getBigDecimal(7));

		marketQuotation.setSymbol(symbolLineDTO.getSymbol());
		marketQuotation.setIntervalEnum(IntervalEnum.fromCode(symbolLineDTO.getInterval()));
		return marketQuotation;
	}

	public static MarketQuotation transformMarketQuotation(String symbol,IntervalEnum intervalEnum,String event) {
		JSONObject entries = JSONUtil.parseObj(event);
		JSONObject k = entries.getJSONObject("k");
		MarketQuotation marketQuotation = new MarketQuotation();
		marketQuotation.setSymbol(symbol);
		marketQuotation.setIntervalEnum(intervalEnum);
		marketQuotation.setOpen(k.getDouble("o"));
		marketQuotation.setBigDecimalOpen(k.getBigDecimal("o"));
		marketQuotation.setHigh(k.getDouble("h"));
		marketQuotation.setBigDecimalHigh(k.getBigDecimal("h"));
		marketQuotation.setLow(k.getDouble("l"));
		marketQuotation.setBigDecimalLow(k.getBigDecimal("l"));
		marketQuotation.setClose(k.getDouble("c"));
		marketQuotation.setBigDecimalClose(k.getBigDecimal("c"));
		marketQuotation.setVolume(k.getDouble("v"));
		marketQuotation.setBigDecimalVolume(k.getBigDecimal("v"));
		marketQuotation.setX(k.getBool("x"));
		return marketQuotation;
	}
}
