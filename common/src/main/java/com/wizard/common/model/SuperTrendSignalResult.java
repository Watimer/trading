package com.wizard.common.model;

import com.wizard.common.service.SuperTrendSignalService.SignalCombination;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 超级趋势信号分析结果
 */
@Data
@Builder
public class SuperTrendSignalResult {
    /** 交易对 */
    private String symbol;

    /** 日线趋势 */
    private TrendDirection dailyTrend;

    /** 4小时趋势 */
    private TrendDirection fourHourTrend;

    /** 1小时趋势 */
    private TrendDirection oneHourTrend;

    /** 15分钟趋势 */
    private TrendDirection fifteenMinTrend;

    /** 信号组合类型 */
    private SignalCombination signalCombination;

    /** 信号级别 */
    private SignalLevel signalLevel;

    /** 通知内容 */
    private SignalNotification notification;

    /** 分析时间 */
    private LocalDateTime analysisTime;
}