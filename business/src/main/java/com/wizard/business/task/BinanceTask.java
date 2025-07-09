package com.wizard.business.task;

import com.wizard.business.service.BusinessService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author wizard
 * @date 2025年07月09日 18:10
 * @desc
 */
@Configuration
@EnableScheduling
public class BinanceTask {

	@Resource
	BusinessService businessService;

	/**
	 * 从零时起,每四小时零2秒执行一次
	 */
	@Scheduled(cron = "2 0 0/4 * * ?")
	public void scanFourHourData(){
		//businessService.marketQuotationList()
	}

}
