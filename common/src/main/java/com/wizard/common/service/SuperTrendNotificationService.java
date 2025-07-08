package com.wizard.common.service;

import com.wizard.common.enums.IntervalEnum;
import com.wizard.common.model.MarketQuotation;
import com.wizard.common.model.SuperTrendSignalResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 超级趋势信号通知推送服务
 * 负责分析信号并推送通知
 */
@Slf4j
@Service
public class SuperTrendNotificationService {

    @Autowired
    private SuperTrendSignalService signalService;

    // 注释掉PushService的注入，使用接口方法
    // @Autowired
    // private PushService pushService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 分析并推送超级趋势信号通知
     *
     * @param symbol        交易对
     * @param timeFrameData 多周期K线数据
     * @return 是否推送成功
     */
    public boolean analyzeAndPushSignal(String symbol, Map<IntervalEnum, List<MarketQuotation>> timeFrameData) {
        try {
            log.info("开始分析{}的超级趋势信号", symbol);

            // 分析信号
            SuperTrendSignalResult result = signalService.analyzeMultiTimeFrameSignal(symbol, timeFrameData);

            // 根据信号级别决定是否推送
            if (shouldPushNotification(result)) {
                return pushNotification(result);
            } else {
                log.info("信号级别为观望，跳过推送: {}", result.getSignalLevel());
                return false;
            }

        } catch (Exception e) {
            log.error("分析和推送超级趋势信号时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 仅分析多个交易对的信号并批量推送概览
     *
     * @param symbolDataMap 多个交易对的多周期数据
     * @return 是否推送成功
     */
    public boolean analyzeMultipleSymbolsAndPush(Map<String, Map<IntervalEnum, List<MarketQuotation>>> symbolDataMap) {
        try {
            StringBuilder overview = new StringBuilder();
            overview.append("📊 多币种超级趋势信号概览\n\n");

            int strongSignalCount = 0;
            int mediumSignalCount = 0;

            for (Map.Entry<String, Map<IntervalEnum, List<MarketQuotation>>> entry : symbolDataMap.entrySet()) {
                String symbol = entry.getKey();
                Map<IntervalEnum, List<MarketQuotation>> timeFrameData = entry.getValue();

                SuperTrendSignalResult result = signalService.analyzeMultiTimeFrameSignal(symbol, timeFrameData);

                // 添加到概览中
                overview.append(formatSymbolOverview(result));

                // 统计强烈和中等信号
                switch (result.getSignalLevel()) {
                    case STRONG:
                        strongSignalCount++;
                        break;
                    case MEDIUM:
                        mediumSignalCount++;
                        break;
                }
            }

            // 添加统计信息
            overview.append(String.format("\n📈 统计：强烈信号 %d 个，中等信号 %d 个", strongSignalCount, mediumSignalCount));
            overview.append(String.format("\n⏰ 分析时间：%s", FORMATTER.format(java.time.LocalDateTime.now())));

            // 推送概览 - 使用回调方式
            return pushNotificationCallback(overview.toString());

        } catch (Exception e) {
            log.error("分析多币种信号时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 判断是否应该推送通知
     * 只有强烈、中等和较弱级别才推送，观望级别不推送
     */
    private boolean shouldPushNotification(SuperTrendSignalResult result) {
        switch (result.getSignalLevel()) {
            case STRONG:
            case MEDIUM:
            case WEAK:
                return true;
            case WAIT:
            default:
                return false;
        }
    }

    /**
     * 推送单个交易对的信号通知
     */
    private boolean pushNotification(SuperTrendSignalResult result) {
        try {
            String content = buildNotificationContent(result);
            boolean pushResult = pushNotificationCallback(content);

            if (pushResult) {
                log.info("成功推送{}的超级趋势信号通知，级别: {}",
                        result.getSymbol(), result.getSignalLevel().getName());
            } else {
                log.error("推送{}的超级趋势信号通知失败", result.getSymbol());
            }

            return pushResult;
        } catch (Exception e) {
            log.error("推送通知时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 构建通知内容
     */
    private String buildNotificationContent(SuperTrendSignalResult result) {
        StringBuilder content = new StringBuilder();

        // 标题
        content.append(String.format("📊 【交易信号 - %s】\n\n", result.getSymbol()));

        // 周期趋势分析
        content.append("🕐 周期趋势分析：\n");
        content.append(String.format("├ 日线：%s %s\n",
                result.getDailyTrend().getSymbol(), result.getDailyTrend().getDescription()));
        content.append(String.format("├ 4小时：%s %s\n",
                result.getFourHourTrend().getSymbol(), result.getFourHourTrend().getDescription()));
        content.append(String.format("├ 1小时：%s %s\n",
                result.getOneHourTrend().getSymbol(), result.getOneHourTrend().getDescription()));
        content.append(String.format("└ 15分钟：%s %s\n\n",
                result.getFifteenMinTrend().getSymbol(), result.getFifteenMinTrend().getDescription()));

        // 信号级别
        content.append(String.format("%s %s级别\n\n",
                result.getSignalLevel().getIcon(), result.getSignalLevel().getName()));

        // 策略建议
        content.append(String.format("%s %s\n\n",
                result.getSignalCombination().getEmoji(),
                result.getSignalCombination().getDescription()));

        // 具体策略
        content.append(String.format("💡 策略建议：%s\n\n",
                result.getSignalCombination().getStrategy()));

        // 时间戳
        content.append(String.format("⏰ 分析时间：%s",
                FORMATTER.format(result.getAnalysisTime())));

        return content.toString();
    }

    /**
     * 格式化单个交易对的概览信息
     */
    private String formatSymbolOverview(SuperTrendSignalResult result) {
        return String.format("%s %s: %s%s%s%s %s %s\n",
                result.getSignalLevel().getIcon(),
                result.getSymbol(),
                result.getDailyTrend().getSymbol(),
                result.getFourHourTrend().getSymbol(),
                result.getOneHourTrend().getSymbol(),
                result.getFifteenMinTrend().getSymbol(),
                result.getSignalCombination().getEmoji(),
                result.getSignalCombination().getDescription());
    }

    /**
     * 推送通知回调方法，子类或调用方可以覆盖此方法实现具体的推送逻辑
     */
    protected boolean pushNotificationCallback(String content) {
        // 默认实现：仅记录日志
        log.info("推送通知内容: {}", content);
        return true;
    }
}
