package com.wizard.business;

import com.binance.connector.futures.client.impl.futures.Account;
import com.wizard.business.component.PushMessage;
import com.wizard.business.service.AccountOrderBinance;
import com.wizard.business.service.TradingViewService;
import com.wizard.common.model.dto.DingDingMessageDTO;
import com.wizard.common.model.dto.MarkdownDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import java.util.LinkedHashMap;

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
		parameters.put("quantity", 300);
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
}
