package com.wizard.business.component;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.binance.connector.futures.client.WebsocketClient;
import com.binance.connector.futures.client.exceptions.BinanceClientException;
import com.binance.connector.futures.client.impl.UMWebsocketClientImpl;
import com.binance.connector.futures.client.impl.WebsocketClientImpl;
import com.binance.connector.futures.client.utils.WebSocketCallback;
import com.wizard.common.component.GlobalListComponent;
import com.wizard.common.component.LikeListComponent;
import com.wizard.common.enums.IntervalEnum;
import com.wizard.common.model.MarketQuotation;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @author wizard
 * @date 2025年07月08日 10:46
 * @desc
 */
@Slf4j
@Component
public class StartComponent {

	@Resource
	GlobalListComponent globalListComponent;

	@Resource
	LikeListComponent likeListComponent;
	List<String> list = Arrays.asList("BTCUSDT", "AAVEUSDT", "SOLUSDT");
	List<IntervalEnum> intervalList = Arrays.asList(IntervalEnum.FIFTEEN_MINUTE, IntervalEnum.ONE_HOUR,IntervalEnum.FOUR_HOUR,IntervalEnum.ONE_DAY);

	//@PostConstruct
	public void initGlobalList(){
		Long logId = IdWorker.getId();
		log.info("日志ID:{},启动时执行1",logId);

		globalListComponent.addToGlobalList(logId,list);
		log.info("日志ID:{},数据初始化完成..",logId);
		list.stream().forEach(symbol -> {
			likeListComponent.addOptional(logId,symbol,null);
		});
		initWebSocket();
	}

	public void initWebSocket(){
		WebsocketClientImpl websocketClient = new UMWebsocketClientImpl();
		list.stream().forEach(symbol -> {
			intervalList.stream().forEach(interval -> {
				websocketClient.klineStream(symbol, interval.getCode(),(event) ->{
					log.info("接收到K线数据:{}",symbol);
					MarketQuotation marketQuotation = JSONUtil.toBean(event, MarketQuotation.class);
					likeListComponent.addOptionalMarketQuotation(1L,symbol,interval,marketQuotation);
				});
			});
		});

	}
}
