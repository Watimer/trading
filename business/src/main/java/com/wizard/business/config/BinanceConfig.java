package com.wizard.business.config;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.binance.connector.futures.client.impl.futures.Account;
import com.binance.connector.futures.client.impl.um_futures.UMAccount;
import com.binance.connector.futures.client.impl.um_futures.UMMarket;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wizard
 * @date 2025年07月09日 17:18
 * @desc
 */
@Configuration
public class BinanceConfig {

	private static final String BINANCE_PRODUCT_URL = "https://fapi.binance.com/fapi";

	@Bean(name = "binanceFuturesClient")
	public UMFuturesClientImpl binanceFuturesClient() {
		return new UMFuturesClientImpl();
	}

	@Bean(name = "binanceFuturesAccount")
	public Account binanceFuturesAccount() {
		return new UMAccount(BINANCE_PRODUCT_URL,
				System.getenv("BINANCE_TRADE_API_KEY"),
				System.getenv("BINANCE_TRADE_SECRET_KEY"),
				true,null);
	}

	@Bean(name = "binanceUMMarket")
	public UMMarket binanceUMMarket() {
		return new UMMarket(BINANCE_PRODUCT_URL,
				System.getenv("BINANCE_TRADE_API_KEY"),
				System.getenv("BINANCE_TRADE_SECRET_KEY"),
				true,
				null);
	}
}
