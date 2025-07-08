package com.wizard.business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author wizard
 * @date 2025年07月08日 10:28
 * @desc
 */
@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = {"com.wizard.**"})
public class BusinessApplication {

	public static void main(String[] args) {
		SpringApplication.run(BusinessApplication.class, args);
		log.info("BusinessApplication started");
	}
}
