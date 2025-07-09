package com.wizard.business.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.binance.connector.futures.client.impl.futures.Market;
import com.binance.connector.futures.client.impl.um_futures.UMMarket;
import com.wizard.common.enums.ContractTypeEnum;
import com.wizard.common.enums.IntervalEnum;
import com.wizard.common.model.MarketQuotation;
import com.wizard.common.model.dto.SymbolLineDTO;
import com.wizard.common.utils.DataTransformationUtil;
import com.wizard.common.utils.IndicatorCalculateUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author wizard
 * @date 2025年07月09日 17:10
 * @desc 业务类
 */
@Slf4j
@Service
public class BusinessService {


	@Resource
	@Qualifier("binanceFuturesClient")
	UMFuturesClientImpl binanceFuturesClient;

	/**
	 * 获取K线及计算指标
	 * @param symbolLineDTO
	 * @return
	 */
	public List<MarketQuotation> marketQuotationList(SymbolLineDTO symbolLineDTO) {
		// 开始拼接参数,默认请求500条K
		if(ObjectUtil.isNull(symbolLineDTO.getLimit()) || symbolLineDTO.getLimit() <= 0){
			symbolLineDTO.setLimit(500);
		}
		LinkedHashMap<String, Object> params = new LinkedHashMap<>();
		// 标的
		params.put("pair", symbolLineDTO.getSymbol());
		// 数据类型-默认永续合约
		if(StrUtil.isBlank(symbolLineDTO.getContractType())){
			symbolLineDTO.setContractType(ContractTypeEnum.PERPETUAL.getCode());
		}
		params.put("contractType", symbolLineDTO.getContractType());
		// 周期-默认15min
		if (StrUtil.isBlank(symbolLineDTO.getInterval()))
			symbolLineDTO.setInterval(IntervalEnum.FIFTEEN_MINUTE.getCode());
		params.put("interval", symbolLineDTO.getInterval());
		params.put("limit", symbolLineDTO.getLimit());
		// 调用接口,获取K线数据
		String continuousKLines = binanceFuturesClient.market().continuousKlines(params);
		// 将原始数据转化
		List<MarketQuotation> marketQuotationList = DataTransformationUtil.transform(symbolLineDTO, continuousKLines);
		// 计算指标
		IndicatorCalculateUtil.multipleIndicatorCalculate(marketQuotationList,2);

		return marketQuotationList;
	}
}
