package com.wizard.business.service;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.wizard.business.component.PushMessage;
import com.wizard.business.component.RedisUtils;
import com.wizard.common.constants.RedisConstants;
import com.wizard.common.enums.IndicatorEnum;
import com.wizard.common.model.dto.DingDingMessageDTO;
import com.wizard.common.utils.SuperTrend;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.wizard.common.component.LikeListComponent;
import com.wizard.common.enums.ContractTypeEnum;
import com.wizard.common.enums.IntervalEnum;
import com.wizard.common.model.MarketQuotation;
import com.wizard.common.model.SuperTrendSignalResult;
import com.wizard.common.model.dto.SymbolLineDTO;
import com.wizard.common.service.SuperTrendSignalService;
import com.wizard.common.utils.DataTransformationUtil;
import com.wizard.common.utils.IndicatorCalculateUtil;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import static com.wizard.common.constants.RedisConstants.BINANCE_SYMBOL;

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

    @Resource
    RedisUtils redisUtils;

    @Resource
    private LikeListComponent likeListComponent;

    @Resource
    PushMessage pushMessage;

    /**
     * 内存存储结构：存储多标的、多周期的超级趋势指标数据
     * 结构：Map<symbol, Map<interval, SuperTrendSignalResult>>
     */
    private final Map<String, Map<IntervalEnum, SuperTrendSignalResult>> superTrendDataCache = new HashMap<>();

    /**
     * 数据缓存
     */
    private final Map<String,Map<String,String>> symbolDataCache = new HashMap<>();

    /**
     * 获取K线及计算指标
     *
     * @param symbolLineDTO
     * @return
     */
    public List<MarketQuotation> marketQuotationList(SymbolLineDTO symbolLineDTO) {
        // 开始拼接参数,默认请求500条K
        if (ObjectUtil.isNull(symbolLineDTO.getLimit()) || symbolLineDTO.getLimit() <= 0) {
            symbolLineDTO.setLimit(500);
        }
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        // 标的
        params.put("pair", symbolLineDTO.getSymbol());
        // 数据类型-默认永续合约
        if (StrUtil.isBlank(symbolLineDTO.getContractType())) {
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
        IndicatorCalculateUtil.multipleIndicatorCalculate(marketQuotationList, 2);

        return marketQuotationList;
    }

    /**
     * 扫描周期
     */
    private final List<IntervalEnum> intervalList = Arrays.asList(IntervalEnum.FIFTEEN_MINUTE,IntervalEnum.ONE_HOUR,IntervalEnum.FOUR_HOUR,IntervalEnum.ONE_DAY);



    /**
     * 扫描自选数据及周期,并计算指标,保存指标值
     */
    public void scanFourHourData() {
        String symbolString = redisUtils.get(RedisConstants.OPTIONAL_SYMBOL);
        if(StrUtil.isBlank(symbolString)) {
            return;
        }
        List<String> symbolList = JSONUtil.toList(symbolString,String.class);
        // 遍历自选数据
        symbolList.stream().forEach(symbol -> {
            // 遍历周期
            intervalList.stream().forEach(interval -> {
                // 构建请求参数
                SymbolLineDTO symbolLineDTO = SymbolLineDTO.builder()
                        .symbol(symbol)
                        .interval(interval.getCode())
                        .contractType(ContractTypeEnum.PERPETUAL.getCode())
                        .limit(500) // 获取500条K线数据
                        .build();
                // 获取K线数据并计算指标
                List<MarketQuotation> marketQuotationList = marketQuotationList(symbolLineDTO);

                if(CollUtil.isEmpty(marketQuotationList)) {
                    log.warn("标的 {} 的 {} 周期数据为空", symbol, interval.getCode());
                } else {
                    MarketQuotation marketQuotation = marketQuotationList.get(marketQuotationList.size() -1);
                    SuperTrend superTrend = marketQuotation.getSupertrendIndicator();
                    String key = symbol+":"+ interval;
                    String indicatorName = IndicatorEnum.SUPER_TREND.getCode();
                    redisUtils.hSet(key, indicatorName,JSONUtil.toJsonStr(superTrend));
                }
            });
        });
    }

    /**
     * 获取指定标的和周期的超级趋势数据
     */
    public SuperTrendSignalResult getSupertrendData(String symbol, IntervalEnum interval) {
        Map<IntervalEnum, SuperTrendSignalResult> symbolData = superTrendDataCache.get(symbol);
        if (symbolData != null) {
            return symbolData.get(interval);
        }
        return null;
    }

    /**
     * 获取指定标的的所有周期超级趋势数据
     */
    public Map<IntervalEnum, SuperTrendSignalResult> getSupertrendDataBySymbol(String symbol) {
        return superTrendDataCache.get(symbol);
    }

    /**
     * 获取所有标的的超级趋势数据
     */
    public Map<String, Map<IntervalEnum, SuperTrendSignalResult>> getAllSupertrendData() {
        return new HashMap<>(superTrendDataCache);
    }

    /**
     * 添加自选
     * @param symbol
     * @return
     */
    public Boolean addOptionalSymbol(String symbol) {
        if(!symbol.contains("USDT")){
            symbol = symbol + "USDT";
        }
        List<String> symbolList = new ArrayList<>();
        String temp = redisUtils.get(RedisConstants.OPTIONAL_SYMBOL);
        if(StrUtil.isBlank(temp)) {
            symbolList.add(symbol);
        } else {
            symbolList = JSON.parseArray(temp,String.class);
            if(!symbolList.contains(symbol)) {
                symbolList.add(symbol);
            }
        }
        redisUtils.set(RedisConstants.OPTIONAL_SYMBOL, JSON.toJSONString(symbolList));
        return Boolean.TRUE;
    }

    /**
     * 获取自选
     * @return
     */
    public List<String> getOptionalSymbol() {
        String temp = redisUtils.get(RedisConstants.OPTIONAL_SYMBOL);
        if(StrUtil.isBlank(temp)){
            return Collections.emptyList();
        }
        return JSON.parseArray(temp,String.class);
    }

    /**
     * 计算结果
     * @param marketQuotation       最新K
     * @param indicatorEnum         待计算的指标信号
     */
    public void calculate(MarketQuotation marketQuotation, IndicatorEnum indicatorEnum) {
        String symbol = marketQuotation.getSymbol();
        String indicatorName = indicatorEnum.getCode();
        AtomicBoolean pushFlag = new AtomicBoolean(false);
        DingDingMessageDTO dingDingMessageDTO = DingDingMessageDTO.builder()
                .msgtype("text")
                .build();
        StringBuilder content = new StringBuilder();
        // 超级趋势策略信号
        if(IndicatorEnum.SUPER_TREND.equals(indicatorEnum)) {
            intervalList.stream().forEach(interval -> {
                String key = symbol+":"+interval;
                String string = redisUtils.hGet(key, indicatorName);
                SuperTrend superTrend = JSON.parseObject(string,SuperTrend.class);
                boolean isUptrend = superTrend.getIsUptrend();
                // SUPER_TREND 之上
                if(isUptrend){
                    double tempLow = marketQuotation.getLow() - superTrend.getSupertrendValue();
                    double tempLowRate = Math.abs(tempLow) / superTrend.getSupertrendValue();
                    double tempClose = marketQuotation.getClose() - superTrend.getSupertrendValue();
                    double tempCloseRate = Math.abs(tempClose) / superTrend.getSupertrendValue();
                    if((tempLow>=0 && tempLowRate <= 0.02) || (tempClose >= 0 && tempCloseRate <= 0.02)){
                        pushFlag.set(true);
                        content.append(symbol).append("\n")
                                .append("价格:").append(" ").append(marketQuotation.getBigDecimalClose()).append("\n")
                                .append("指标:").append(" ").append(indicatorName).append("\n")
                                .append("周期:").append(" ").append(interval).append("\n")
                                .append("方向:").append(" ").append("支撑之上").append("\n")
                                .append("建议:").append(" ").append("回踩至支撑附近,适当做多。").append("\n");
                    }
                }  else {
                    double tempHigh = superTrend.getSupertrendValue() - marketQuotation.getHigh();
                    double tempHighRate = Math.abs(tempHigh) / superTrend.getSupertrendValue();
                    double tempClose = superTrend.getSupertrendValue() - marketQuotation.getClose();
                    double tempCloseRate = Math.abs(tempClose) / superTrend.getSupertrendValue();
                    if((tempHigh >= 0 && tempHighRate <= 0.02) || (tempClose >= 0 && tempCloseRate <= 0.02)){
                        pushFlag.set(true);
                        content.append(symbol).append("\n")
                                .append("价格:").append(" ").append(marketQuotation.getBigDecimalClose()).append("\n")
                                .append("指标:").append(" ").append(indicatorName).append("\n")
                                .append("周期:").append(" ").append(interval).append("\n")
                                .append("方向:").append(" ").append("阻力之下").append("\n")
                                .append("建议:").append(" ").append("反弹至阻力附近,适当做空。").append("\n");
                    }
                }
            });
        }
        // 其他信号，待补充

        // 发送通知
        if(pushFlag.get()){
            dingDingMessageDTO.setContext(content.toString());
            pushMessage.pushMessage(dingDingMessageDTO);
        }
    }

    /**
     * 扫描币安可交易标的
     */
    public void scanAllSymbol() {

        String string = binanceFuturesClient.market().exchangeInfo();

        JSONObject jsonObject = JSONObject.parseObject(string);
        JSONArray jsonArray = jsonObject.getJSONArray("symbols");
        List<String> resultList = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
            // 获取合约类型
            String contractType = jsonObject1.getString("contractType");
            // 获取交易对名称
            String symbol = jsonObject1.getString("symbol");
            if("PERPETUAL".equals(contractType)&& !symbol.contains("USDC") && symbol.contains("USDT")){
                resultList.add(symbol);
            }
        }
        redisUtils.set(RedisConstants.BINANCE_SYMBOL,JSONObject.toJSONString(resultList));
    }

    public Boolean testPushMessage() {
        DingDingMessageDTO dingDingMessageDTO = DingDingMessageDTO.builder()
                .msgtype("text")
                .context("这是一条测试消息")
                .build();
        pushMessage.pushMessage(dingDingMessageDTO);
        return true;
    }
}
