package com.wizard.business.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wizard.business.service.BusinessService;
import com.wizard.common.base.ResultInfo;
import com.wizard.common.enums.IntervalEnum;
import com.wizard.common.model.MarketQuotation;
import com.wizard.common.model.SuperTrendSignalResult;
import com.wizard.common.model.dto.SymbolLineDTO;
import com.wizard.common.utils.ResultInfoUtil;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wizard
 * @date 2025年07月09日 17:04
 * @desc
 */
@Slf4j
@RestController
public class BusinessController {

    @Resource
    HttpServletRequest request;

    @Resource
    BusinessService businessService;

    /**
     * 查询指定标的的K线数据
     *
     * @param symbolLineDTO 请求参数
     */
    @GetMapping("/getMarketQuotationList")
    public ResultInfo<java.util.List<MarketQuotation>> marketQuotationList(SymbolLineDTO symbolLineDTO) {
        ResultInfo<java.util.List<MarketQuotation>> resultInfo = null;
        try {
            java.util.List<MarketQuotation> data = businessService.marketQuotationList(symbolLineDTO);
            resultInfo = ResultInfoUtil.buildSuccess(request.getRequestURI(), data);
        } catch (Exception e) {
            resultInfo = ResultInfoUtil.buildErrorMsg(e.getMessage());
        }
        return resultInfo;
    }

    @GetMapping("/addOptionalSymbol")
    public ResultInfo<Boolean> addOptionalSymbol(String symbol) {
        ResultInfo<Boolean> resultInfo = null;
        try {
            Boolean data = businessService.addOptionalSymbol(symbol);
            resultInfo = ResultInfoUtil.buildSuccess(request.getRequestURI(), data);
        } catch (Exception e) {
            resultInfo = ResultInfoUtil.buildErrorMsg(e.getMessage());
        }
        return resultInfo;
    }

    @GetMapping("/getOptionalSymbol")
    public ResultInfo<List<String>> getOptionalSymbol() {
        ResultInfo<List<String>> resultInfo = null;
        try {
            List<String> data = businessService.getOptionalSymbol();
            resultInfo = ResultInfoUtil.buildSuccess(request.getRequestURI(), data);
        } catch (Exception e) {
            resultInfo = ResultInfoUtil.buildErrorMsg(e.getMessage());
        }
        return resultInfo;
    }

    /**
     * 扫描自选数据及周期，计算超级趋势指标
     */
    @GetMapping("/scanSupertrendData")
    public ResultInfo<String> scanSupertrendData() {
        try {
            log.info("开始扫描超级趋势数据");
            businessService.scanFourHourData();
            return ResultInfoUtil.buildSuccess(request.getRequestURI(), "扫描完成，超级趋势数据已更新到内存缓存");
        } catch (Exception e) {
            log.error("扫描超级趋势数据失败: {}", e.getMessage(), e);
            return ResultInfoUtil.buildErrorMsg("扫描失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定标的和周期的超级趋势数据
     */
    @GetMapping("/getSupertrendData")
    public ResultInfo<SuperTrendSignalResult> getSupertrendData(
            @RequestParam String symbol,
            @RequestParam String interval) {
        try {
            IntervalEnum intervalEnum = IntervalEnum.fromCode(interval);
            SuperTrendSignalResult data = businessService.getSupertrendData(symbol, intervalEnum);

            if (data != null) {
                return ResultInfoUtil.buildSuccess(request.getRequestURI(), data);
            } else {
                return ResultInfoUtil.buildErrorMsg("未找到指定标的和周期的超级趋势数据");
            }
        } catch (Exception e) {
            log.error("获取超级趋势数据失败: {}", e.getMessage(), e);
            return ResultInfoUtil.buildErrorMsg("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定标的的所有周期超级趋势数据
     */
    @GetMapping("/getSupertrendDataBySymbol")
    public ResultInfo<Map<IntervalEnum, SuperTrendSignalResult>> getSupertrendDataBySymbol(
            @RequestParam String symbol) {
        try {
            Map<IntervalEnum, SuperTrendSignalResult> data = businessService.getSupertrendDataBySymbol(symbol);

            if (data != null && !data.isEmpty()) {
                return ResultInfoUtil.buildSuccess(request.getRequestURI(), data);
            } else {
                return ResultInfoUtil.buildErrorMsg("未找到指定标的的超级趋势数据");
            }
        } catch (Exception e) {
            log.error("获取指定标的超级趋势数据失败: {}", e.getMessage(), e);
            return ResultInfoUtil.buildErrorMsg("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有标的的超级趋势数据
     */
    @GetMapping("/getAllSupertrendData")
    public ResultInfo<Map<String, Map<IntervalEnum, SuperTrendSignalResult>>> getAllSupertrendData() {
        try {
            Map<String, Map<IntervalEnum, SuperTrendSignalResult>> data = businessService.getAllSupertrendData();
            return ResultInfoUtil.buildSuccess(request.getRequestURI(), data);
        } catch (Exception e) {
            log.error("获取所有超级趋势数据失败: {}", e.getMessage(), e);
            return ResultInfoUtil.buildErrorMsg("获取失败: " + e.getMessage());
        }
    }


    @GetMapping("/testPushMessage")
    public ResultInfo<Boolean> testPushMessage(){
        ResultInfo<Boolean> resultInfo = null;
        try {
            Boolean data = businessService.testPushMessage();
            return ResultInfoUtil.buildSuccess(request.getRequestURI(), data);
        } catch (Exception e) {
            return ResultInfoUtil.buildErrorMsg("获取失败: " + e.getMessage());
        }
    }
}
