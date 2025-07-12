package com.wizard.business.task;

import com.wizard.business.service.BusinessService;
import com.wizard.business.service.TradingViewService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author wizard
 * @date 2025年07月09日 18:10
 * @desc
 */
@Slf4j
@Configuration
@EnableScheduling
public class BinanceTask {

    @Resource
    BusinessService businessService;

	@Resource
	TradingViewService tradingViewService;

    /**
	 * 从零时起,每四小时零2秒执行一次
	 */
	//@Scheduled(fixedRate = 1000 * 60 * 5)
	public void scanFourHourData(){
		businessService.scanFourHourData();
	}

	/**
	 * 扫描所有的可交易标的
	 */
	//@Scheduled(fixedRate = 1000 * 60 * 15)
	public void scanAllSymbol(){
		businessService.scanAllSymbol();
	}

	/**
	 * 调用tv筛选器
	 */
	@Scheduled(cron = "2 0 0/1 * * ?")
	public void scanTradingView(){
		log.info("开始执行TV检测");
		tradingViewService.scan();
		log.info("完成执行TV检测");
	}
}
