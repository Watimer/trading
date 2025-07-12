package com.wizard.business.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.binance.connector.futures.client.impl.futures.Account;
import com.binance.connector.futures.client.impl.futures.Market;
import com.binance.connector.futures.client.impl.um_futures.UMMarket;
import com.wizard.business.component.PushMessage;
import com.wizard.business.component.RedisUtils;
import com.wizard.common.component.GlobalListComponent;
import com.wizard.common.constants.RedisConstants;
import com.wizard.common.enums.NewsTypeEnum;
import com.wizard.common.model.dto.DingDingMessageDTO;
import com.wizard.common.utils.TokenNewsAnalyzerUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author wizard
 * @date 2025年07月11日 15:52
 * @desc
 */
@Slf4j
@Service
public class NewsService {

	@Resource
	RedisUtils redisUtils;

	@Resource
	UMMarketService umMarketService;

	@Resource
	AccountOrderBinance accountOrderBinance;

	@Resource
	PushMessage pushMessage;

	@Value("${newsAddress}")
	private String NEWS_ADDRESS;

	/**
	 * 监控新闻列表
	 */
	public void pullNews() {
		while (true) {
			// 随机生成睡眠时间
			int sleepSecond = RandomUtil.randomInt(1,7);
			ThreadUtil.sleep(sleepSecond, TimeUnit.SECONDS);
			List<String> symbolList = new ArrayList<>();

			// 新闻地址
			//String newsAddress = "https://api.panewslab.com/webapi/flashnews?rn=20&lid=1&apppush=0";
			// 调用新闻接口
			String body = HttpRequest.get(NEWS_ADDRESS).execute().body();
			if(StrUtil.isBlank(body)){
				// 发送系统通知
				pushMessage.pushText("新闻结果为空");
				continue;
			}
			JSONObject jsonObject = null;
			try {
				jsonObject = JSONObject.parseObject(body);
			} catch (Exception e) {
				// JSON解析失败,发送通知
				pushMessage.pushText("新闻解析失败"+e.getMessage());
				continue;
			}
			TokenNewsAnalyzerUtil.TokenNewsResult tokenNewsResult = null;
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

						tokenNewsResult = TokenNewsAnalyzerUtil.analyzeTokenNews(string);
					}
				}
			}

			// 根据新闻解析结果,执行下单操作
			if(ObjectUtil.isNotNull(tokenNewsResult)){
				// 新闻解析方向
				NewsTypeEnum newsType = tokenNewsResult.getNewsType();
				// 解析出的代币列表
				symbolList = tokenNewsResult.getExtractedTokens();

				switch (newsType) {
					// 上架
					case LISTING:
						extractedBinance(symbolList,"BUY","LONG");
						break;
					// 下架
					case DELISTING:
						extractedBinance(symbolList,"SELL","SHORT");
						break;
					// 未知
					default:
						break;
				}
			}
		}

	}

	/**
	 * 币安下单操作
	 * @param symbolList		币种列表
	 * @param side				买或卖 BUY OR SELL
	 * @param positionSide		多或空 LONG OR SHORT
	 */
	private void extractedBinance(List<String> symbolList,String side,String positionSide) {
		if(CollUtil.isEmpty(symbolList)){
			return;
		}
		symbolList.stream().forEach(symbol -> {
			if(!symbol.contains("USDT")){
				symbol = symbol + "USDT";
			}
			String binanceSymbolList = redisUtils.get(RedisConstants.BINANCE_SYMBOL);
			if(StrUtil.isNotBlank(binanceSymbolList) && binanceSymbolList.contains(symbol)){

				// 最低数量为5U
				BigDecimal minMoney = new BigDecimal("5.0");
				// 当前价格
				BigDecimal price = umMarketService.getMarketPrice(symbol);

				// 计算最低买入数量
				BigDecimal bigDecimalCount = minMoney.divide(price, RoundingMode.HALF_DOWN);
				// 最低买入数量 * 2
				bigDecimalCount = bigDecimalCount.multiply(new BigDecimal(symbol));

				LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
				parameters.put("symbols", symbol);
				parameters.put("side", side);
				parameters.put("type", "MARKET");
				parameters.put("positionSide", positionSide);
				parameters.put("quantity", bigDecimalCount.intValue());
				accountOrderBinance.newOrder(parameters);
			}
		});
	}
}
