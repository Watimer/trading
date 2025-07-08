package com.wizard.business.service;

import com.binance.connector.futures.client.WebsocketClient;
import com.binance.connector.futures.client.impl.CMWebsocketClientImpl;
import com.binance.connector.futures.client.impl.FuturesClientImpl;
import com.binance.connector.futures.client.impl.UMWebsocketClientImpl;
import com.binance.connector.futures.client.impl.WebsocketClientImpl;
import com.binance.connector.futures.client.utils.WebSocketCallback;
import com.binance.connector.futures.client.utils.WebSocketConnection;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author wizard
 * @date 2025年07月08日 11:07
 * @desc
 */

@Slf4j
@Service
public abstract class WebSocketService implements WebsocketClient {

	@Override
	public int allTickerStream(WebSocketCallback webSocketCallback) {
		WebsocketClientImpl websocketClient = new UMWebsocketClientImpl();
		websocketClient.allTickerStream((event) -> {
			System.out.println("接收到K线数据：" + event);
		});
		return 0;
	}
}
