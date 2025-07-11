package com.wizard.business.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.binance.connector.futures.client.impl.futures.Account;
import com.wizard.business.component.PushMessage;
import com.wizard.common.model.dto.DingDingMessageDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.LinkedHashMap;

/**
 * @author wizard
 * @date 2025年07月11日 17:11
 * @desc
 */
@Slf4j
@Service
public class AccountOrderBinance {

	@Resource
	PushMessage pushMessage;

	@Resource
	@Qualifier("binanceFuturesAccount")
	Account binanceFuturesAccount;

	/**
	 * 下单方法
	 * @param parameters
	 */
	public void newOrder(LinkedHashMap<String, Object> parameters) {
		String result = null;
		DingDingMessageDTO dingDingMessageDTO = DingDingMessageDTO.builder()
				.msgtype("text")
				.build();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("币安API自动下单通知").append("\n");
		try {
			result = binanceFuturesAccount.newOrder(parameters);
			JSONObject jsonObject = JSONObject.parseObject(result);
			if(ObjectUtil.isNotNull(jsonObject) && ObjectUtil.isNotNull(jsonObject.getJSONObject("data"))) {
				JSONObject data = jsonObject.getJSONObject("data");
				if(ObjectUtil.isNotNull(data) && "NEW".equals(data.getString("status"))) {
					// 发送下单成功的通知
					stringBuilder.append("标的:").append(" ").append(parameters.get("symbol").toString()).append("\n")
							.append("金额:").append(" ").append(data.getString("executedQty")).append("\n")
							.append("均价:").append(" ").append(data.getString("avgPrice")).append("\n")
							.append("成交时间:").append(" ").append(LocalDateTimeUtil.of(data.getLong("updateTime"),ZoneId.of("CTT"))).append("\n");

					dingDingMessageDTO.setContext(stringBuilder.toString());
					pushMessage.pushMessage(dingDingMessageDTO);
				}
			}
		} catch (Exception e) {
			log.info("自动下单失败:{}", e.getMessage());
			// 发送下单失败的通知
			stringBuilder.append("标的:").append(" ").append(parameters.get("symbol").toString()).append("\n")
					.append("金额:").append(" ").append("下单失败").append("\n");
			dingDingMessageDTO.setContext(stringBuilder.toString());
			pushMessage.pushMessage(dingDingMessageDTO);
		}
	}
}
