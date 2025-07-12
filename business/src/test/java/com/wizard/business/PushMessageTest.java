package com.wizard.business;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.wizard.business.component.PushMessage;
import com.wizard.business.service.AccountOrderBinance;
import com.wizard.business.service.TradingViewService;
import com.wizard.business.service.UMMarketService;
import com.wizard.common.model.MarketQuotation;
import com.wizard.common.model.dto.DingDingMessageDTO;
import com.wizard.common.model.dto.MarkdownDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author wizard
 * @date 2025年07月10日 15:58
 * @desc
 */
@Slf4j
@SpringBootTest
public class PushMessageTest {

	@Resource
	PushMessage pushMessage;

	@Resource
	AccountOrderBinance accountOrderBinance;

	@Resource
	TradingViewService tradingViewService;

	@Test
	public void testPushMessage(){
		MarkdownDTO markdownDTO = MarkdownDTO.builder()
				.title("测试MarkDown消息")
				.text("""
| ID | 用户名 | 状态 |
|:---:|---|---|
| 1 | 张三 | 活跃 |
| 2 | 李四 | 休眠 |
| 3 | 王五 | 活跃 |""")
				.build();
		DingDingMessageDTO dingDingMessageDTO = DingDingMessageDTO.builder()
				.msgtype("markdown")
				.markdown(markdownDTO)
				.build();
		pushMessage.pushMessage(dingDingMessageDTO);
	}

	@Test
	public void testScan(){
		tradingViewService.scan();
	}

	@Test
	public void testOrder(){
		LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
		parameters.put("symbol", "PEOPLEUSDT");
		parameters.put("side", "BUY");
		parameters.put("type", "MARKET");
		parameters.put("positionSide", "LONG");
		parameters.put("quantity", 250);
		//parameters.put("timeInForce", "GTC");
		accountOrderBinance.newOrder(parameters);
	}

	/**
	 * 测试环境变量
	 */
	@Test
	public void testSystem(){
		String binanceTradeApiKey = System.getenv("BINANCE_TRADE_API_KEY");
		String binanceTradeSecretKey = System.getenv("BINANCE_TRADE_SECRET_KEY");
		log.info("binanceTradeApiKey:{}", binanceTradeApiKey);
		log.info("binanceTradeSecretKey:{}", binanceTradeSecretKey);
	}

	@Resource
	UMMarketService umMarketService;

	@Test
	public void testPrice(){
		BigDecimal price = umMarketService.getMarketPrice("PEOPLEUSDT");
		BigDecimal minPrice = new BigDecimal("5.0");

		BigDecimal count =  minPrice.divide(price, 2, BigDecimal.ROUND_HALF_UP);

		log.info(count.toPlainString());
	}

	@Resource
	UMFuturesClientImpl umFuturesClientImpl;

	@Test
	public void testExchangeInfo(){
		String string = umFuturesClientImpl.market().exchangeInfo();

		JSONObject jsonObject = JSONObject.parseObject(string);
		JSONArray jsonArray = jsonObject.getJSONArray("symbols");
		List<String> resultList = new ArrayList<>();
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject jsonObject1 = jsonArray.getJSONObject(i);
			// 获取合约类型
			String contractType = jsonObject1.getString("contractType");
			// 获取交易对名称
			String symbol = jsonObject1.getString("symbol");
			if("PERPETUAL".equals(contractType)&& !symbol.contains("USDC") && symbol.contains("USDT")){
				resultList.add(symbol);
			}
		}

		log.info("{}",JSONObject.toJSONString(resultList));
	}
}
