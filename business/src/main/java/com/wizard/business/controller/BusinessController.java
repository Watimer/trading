package com.wizard.business.controller;

import com.wizard.business.service.BusinessService;
import com.wizard.common.base.ResultInfo;
import com.wizard.common.model.MarketQuotation;
import com.wizard.common.model.dto.SymbolLineDTO;
import com.wizard.common.utils.ResultInfoUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author wizard
 * @date 2025年07月09日 17:04
 * @desc
 */
@RestController
public class BusinessController {

	@Resource
	HttpServletRequest request;

	@Resource
	BusinessService businessService;

	/**
	 * 查询指定标的的K线数据
	 * @param symbolLineDTO		请求参数
	 */
	@GetMapping("/getMarketQuotationList")
	public ResultInfo<List<MarketQuotation>> marketQuotationList(SymbolLineDTO symbolLineDTO) {
		ResultInfo<List<MarketQuotation>> resultInfo = null;
		try {
			List<MarketQuotation> data = businessService.marketQuotationList(symbolLineDTO);
			resultInfo = ResultInfoUtil.buildSuccess(request.getRequestURI(),data);
		} catch (Exception e) {
			resultInfo = ResultInfoUtil.buildErrorMsg(e.getMessage());
		}
		return resultInfo;
	}


}
