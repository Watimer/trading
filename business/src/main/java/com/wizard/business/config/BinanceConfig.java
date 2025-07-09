package com.wizard.business.config;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wizard
 * @date 2025年07月09日 17:18
 * @desc
 */
@Configuration
public class BinanceConfig {

	@Bean(name = "binanceFuturesClient")
	public UMFuturesClientImpl binanceFuturesClient() {
		return new UMFuturesClientImpl();
	}
}
