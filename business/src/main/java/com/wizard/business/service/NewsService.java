package com.wizard.business.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.binance.connector.futures.client.impl.futures.Account;
import com.binance.connector.futures.client.impl.futures.Market;
import com.binance.connector.futures.client.impl.um_futures.UMMarket;
import com.wizard.business.component.RedisUtils;
import com.wizard.common.component.GlobalListComponent;
import com.wizard.common.utils.TokenNewsAnalyzerUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author wizard
 * @date 2025年07月11日 15:52
 * @desc
 */
@Service
public class NewsService {

	@Resource
	RedisUtils redisUtils;

	@Resource
	@Qualifier("binanceUMMarket")
	UMMarket binanceUMMarket;

	@Resource
	AccountOrderBinance accountOrderBinance;

	/**
	 * 监控新闻列表
	 */
	public void pullNews() {

		List<String> symbolList = new ArrayList<>();

		// 新闻地址
		String newsAddress = "https://api.panewslab.com/webapi/flashnews?rn=20&lid=1&apppush=0";
		// 调用新闻接口
		String body = HttpRequest.get(newsAddress).execute().body();
		if(StrUtil.isBlank(body)){
			//TODO 发送系统通知
			return;
		}
		JSONObject jsonObject = null;
		try {
			jsonObject = JSONObject.parseObject(body);
		} catch (Exception e) {
			// TODO JSON解析失败,发送通知
		}
		if(ObjectUtil.isNotNull(jsonObject) && 0 == jsonObject.getInteger("errno")){
			// 提取data
			JSONObject data = jsonObject.getJSONObject("data");
			// 提取最新新闻
			JSONArray newsArray = data.getJSONArray("news");
			if(ObjectUtil.isNotEmpty(newsArray)){
				JSONObject jsonObject1 = newsArray.getJSONObject(0);
				if(ObjectUtil.isNotNull(jsonObject1)){
					JSONArray jsonArray = jsonObject1.getJSONArray("list");
					JSONObject jsonObject2 = jsonArray.getJSONObject(0);
					// 新闻标题
					String string = jsonObject2.getString("title");

					symbolList = TokenNewsAnalyzerUtil.extractTokenNames(string);
				}
			}
		}

		if(CollUtil.isNotEmpty(symbolList)){
			String symbol = symbolList.get(0) + "USDT";
			// 此处应添加判断是否在币安的逻辑
			String binanceSymbolList = redisUtils.get("BINANCE:SYMBOLS");
			if(StrUtil.isNotBlank(binanceSymbolList) && binanceSymbolList.contains(symbol)){

				LinkedHashMap<String, Object> parametersPrice = new LinkedHashMap<>();
				parametersPrice.put("symbol", symbol);
				// 查看当前价格
				String string = binanceUMMarket.markPrice(parametersPrice);
				LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
				parameters.put("symbols", symbolList.get(0));
				parameters.put("side", "BUY");
				parameters.put("type", "MARKET");
				parameters.put("quantity", 1);
				accountOrderBinance.newOrder(parameters);
			}
		}
	}
}
