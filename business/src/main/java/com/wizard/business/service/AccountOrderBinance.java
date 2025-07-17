package com.wizard.business.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.binance.connector.futures.client.impl.futures.Account;
import com.wizard.business.component.PushMessage;
import com.wizard.common.enums.OrderStatusEnum;
import com.wizard.common.model.dto.DingDingMessageDTO;
import com.wizard.common.model.vo.OrderInfoVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
	 * 查询是否存在订单
	 * @param symbol	标的
	 * @return
	 */
	public boolean existOrder(String symbol,String positionSide) {
		LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
		parameters.put("symbol", symbol);
		String string = binanceFuturesAccount.allOrders(parameters);
		if(StrUtil.isBlank(string)){
			return false;
		}
		JSONObject jsonObject = JSONObject.parseObject(string);
		String dataString = jsonObject.getString("data");
		if(StrUtil.isBlank(dataString)){
			return false;
		}
		List<OrderInfoVO> orderInfoVOList = JSONObject.parseArray(dataString, OrderInfoVO.class);
		if(CollUtil.isNotEmpty(orderInfoVOList)){
			if(StrUtil.isBlank(positionSide)){
				return true;
			}
			AtomicBoolean exist = new AtomicBoolean(false);
			// 判断当前是否存在同方向订单
			orderInfoVOList.stream().forEach(orderInfoVO -> {
				log.info("{}",JSONObject.toJSONString(orderInfoVO));
				if(orderInfoVO.getPositionSide().equals(positionSide) && orderInfoVO.getStatus().equals(OrderStatusEnum.NEW.getCode())) {
					log.info("存在同方向订单, symbol:{}, 订单状态:{} number:{}", symbol, orderInfoVO.getStatus(),orderInfoVO.getPositionSide());
					exist.set(true);
				}
			});
			return exist.get();
		}
		return false;
	}


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
