package com.wizard.common.service;

import com.alibaba.fastjson.JSONObject;
import com.wizard.common.enums.IntervalEnum;
import com.wizard.common.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 超级趋势信号分析服务
 * 根据多周期SuperTrend指标分析趋势信号，生成交易通知
 */
@Slf4j
@Service
public class SuperTrendSignalService {

    /**
     * 分析多周期超级趋势信号
     *
     * @param symbol        交易对
     * @param timeFrameData 多周期K线数据，key为时间周期，value为对应的K线数据列表
     * @return 信号分析结果
     */
    public SuperTrendSignalResult analyzeMultiTimeFrameSignal(String symbol,
            Map<IntervalEnum, List<MarketQuotation>> timeFrameData) {
        log.info("开始分析{}的多周期SuperTrend信号", symbol);

        // 获取各周期的趋势方向
        TrendDirection dailyTrend = getTrendDirection(timeFrameData.get(IntervalEnum.ONE_DAY));
        TrendDirection fourHourTrend = getTrendDirection(timeFrameData.get(IntervalEnum.FOUR_HOUR));
        TrendDirection oneHourTrend = getTrendDirection(timeFrameData.get(IntervalEnum.ONE_HOUR));
        TrendDirection fifteenMinTrend = getTrendDirection(timeFrameData.get(IntervalEnum.FIFTEEN_MINUTE));

        // 根据策略表判断信号组合
        SignalCombination combination = identifySignalCombination(dailyTrend, fourHourTrend, oneHourTrend,
                fifteenMinTrend);

        // 生成通知内容
        SignalNotification notification = generateNotification(symbol, combination, dailyTrend, fourHourTrend,
                oneHourTrend, fifteenMinTrend);
        SuperTrendSignalResult superTrendSignalResult = SuperTrendSignalResult.builder()
                .symbol(symbol)
                .dailyTrend(dailyTrend)
                .fourHourTrend(fourHourTrend)
                .oneHourTrend(oneHourTrend)
                .fifteenMinTrend(fifteenMinTrend)
                .signalCombination(combination)
                .signalLevel(combination.getLevel())
                .notification(notification)
                .analysisTime(LocalDateTime.now())
                .build();
        log.info("{}", JSONObject.toJSONString(superTrendSignalResult));
        return superTrendSignalResult;
    }

    /**
     * 获取单个周期的趋势方向
     * 直接从已计算的supertrend属性读取趋势方向
     */
    private TrendDirection getTrendDirection(List<MarketQuotation> quotations) {
        if (quotations == null || quotations.isEmpty()) {
            return TrendDirection.UNCLEAR;
        }

        // 获取最新的K线数据
        MarketQuotation latest = quotations.get(quotations.size() - 1);

        // 直接读取已计算的supertrend属性
        if (latest.getSupertrend() != null) {
            return latest.getSupertrend().isUptrend() ? TrendDirection.UP : TrendDirection.DOWN;
        }

        // 如果supertrend为空，返回不明确
        log.warn("SuperTrend指标未计算或为空，交易对: {}, 时间: {}",
                latest.getSymbol(), latest.getTimestamp());
        return TrendDirection.UNCLEAR;
    }

    /**
     * 识别信号组合
     */
    private SignalCombination identifySignalCombination(TrendDirection daily, TrendDirection fourHour,
            TrendDirection oneHour, TrendDirection fifteenMin) {

        // 根据策略文档中的10种组合进行匹配
        if (daily == TrendDirection.UP && fourHour == TrendDirection.UP &&
                oneHour == TrendDirection.UP && fifteenMin == TrendDirection.UP) {
            return SignalCombination.STRONG_BULLISH; // ①多周期强趋势共振
        }

        if (daily == TrendDirection.UP && fourHour == TrendDirection.UP &&
                oneHour == TrendDirection.UP && fifteenMin == TrendDirection.DOWN) {
            return SignalCombination.PULLBACK_IN_BULL; // ②短线回调中的多头趋势
        }

        if (daily == TrendDirection.UP && fourHour == TrendDirection.UP &&
                oneHour == TrendDirection.DOWN && fifteenMin == TrendDirection.UP) {
            return SignalCombination.SHORT_TERM_BOUNCE; // ③1H反弹，趋势暂不确认
        }

        if (daily == TrendDirection.UP && fourHour == TrendDirection.DOWN &&
                oneHour == TrendDirection.UP && fifteenMin == TrendDirection.UP) {
            return SignalCombination.HOURLY_STRONG_BUT_DIVERGENT; // ④1H强，但4H背离
        }

        if (daily == TrendDirection.DOWN && fourHour == TrendDirection.DOWN &&
                oneHour == TrendDirection.DOWN && fifteenMin == TrendDirection.DOWN) {
            return SignalCombination.STRONG_BEARISH; // ⑤空头共振趋势
        }

        if (daily == TrendDirection.DOWN && fourHour == TrendDirection.DOWN &&
                oneHour == TrendDirection.DOWN && fifteenMin == TrendDirection.UP) {
            return SignalCombination.SHORT_TERM_REBOUND; // ⑥短线反弹
        }

        if (daily == TrendDirection.DOWN && fourHour == TrendDirection.DOWN &&
                oneHour == TrendDirection.UP && fifteenMin == TrendDirection.UP) {
            return SignalCombination.POTENTIAL_REVERSAL_ATTEMPT; // ⑦短期反转尝试
        }

        if (daily == TrendDirection.DOWN && fourHour == TrendDirection.UP &&
                oneHour == TrendDirection.UP && fifteenMin == TrendDirection.UP) {
            return SignalCombination.POTENTIAL_BOTTOM_REVERSAL; // ⑧潜在底部反转
        }

        if (daily == TrendDirection.UP && fourHour == TrendDirection.UP &&
                oneHour == TrendDirection.DOWN && fifteenMin == TrendDirection.DOWN) {
            return SignalCombination.PULLBACK_CONFIRMATION; // ⑨回调确认中
        }

        // 其他组合或信号不一致的情况
        return SignalCombination.MIXED_SIGNALS; // ⑩信号不一致，风险大
    }

    /**
     * 生成通知内容
     */
    private SignalNotification generateNotification(String symbol, SignalCombination combination,
            TrendDirection daily, TrendDirection fourHour,
            TrendDirection oneHour, TrendDirection fifteenMin) {

        String title = String.format("【交易信号 - %s】", symbol);

        StringBuilder content = new StringBuilder();
        content.append("周期趋势分析：\n");
        content.append(String.format("- 日线：%s\n", getTrendSymbol(daily)));
        content.append(String.format("- 4小时：%s\n", getTrendSymbol(fourHour)));
        content.append(String.format("- 1小时：%s\n", getTrendSymbol(oneHour)));
        content.append(String.format("- 15分钟：%s\n\n", getTrendSymbol(fifteenMin)));

        content.append(combination.getEmoji()).append(" ").append(combination.getDescription());

        return SignalNotification.builder()
                .title(title)
                .content(content.toString())
                .level(combination.getLevel())
                .emoji(combination.getEmoji())
                .recommendation(combination.getStrategy())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 获取趋势符号
     */
    private String getTrendSymbol(TrendDirection trend) {
        switch (trend) {
            case UP:
                return "↑";
            case DOWN:
                return "↓";
            case UNCLEAR:
            default:
                return "/";
        }
    }
    /**
     * 信号组合枚举
     */
    public enum SignalCombination {
        STRONG_BULLISH(SignalLevel.STRONG, "✅", "多周期共振做多信号，建议立即建仓", "坚决做多，分批建仓"),
        PULLBACK_IN_BULL(SignalLevel.MEDIUM, "🔄", "15分钟回调中，建议等待短线企稳再介入", "等待回调结束再做多"),
        SHORT_TERM_BOUNCE(SignalLevel.MEDIUM, "⚠️", "主趋势尚未上转，短多注意止盈", "快进快出短多单"),
        HOURLY_STRONG_BUT_DIVERGENT(SignalLevel.MEDIUM, "🚨", "中期方向背离，短线多单谨慎介入", "快进快出或控制仓位"),
        STRONG_BEARISH(SignalLevel.STRONG, "⛔", "多周期共振做空信号，建议空单建仓", "做空为主，顺势操作"),
        SHORT_TERM_REBOUND(SignalLevel.MEDIUM, "🚫", "短线反弹，空单择机进场", "反弹做空，设好止损"),
        POTENTIAL_REVERSAL_ATTEMPT(SignalLevel.WEAK, "🔄", "1h向上尝试反转，日线空需谨慎", "多单试探介入，关注1h持续性"),
        POTENTIAL_BOTTOM_REVERSAL(SignalLevel.MEDIUM, "🔁", "潜在底部反转，建议轻仓入场观察", "尝试底部建仓，关注突破"),
        PULLBACK_CONFIRMATION(SignalLevel.WAIT, "🔍", "回调未止，暂不入场", "等待趋势重转，再介入"),
        MIXED_SIGNALS(SignalLevel.WAIT, "❔", "信号冲突，趋势方向不明确，建议观望", "不建议操作，观望为主");

        private final SignalLevel level;
        private final String emoji;
        private final String description;
        private final String strategy;

        SignalCombination(SignalLevel level, String emoji, String description, String strategy) {
            this.level = level;
            this.emoji = emoji;
            this.description = description;
            this.strategy = strategy;
        }

        public SignalLevel getLevel() {
            return level;
        }

        public String getEmoji() {
            return emoji;
        }

        public String getDescription() {
            return description;
        }

        public String getStrategy() {
            return strategy;
        }
    }
}
