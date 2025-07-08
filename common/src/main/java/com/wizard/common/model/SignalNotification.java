package com.wizard.common.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 信号通知模型
 */
@Data
@Builder
public class SignalNotification {
    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 信号级别 */
    private SignalLevel level;

    /** 表情符号 */
    private String emoji;

    /** 策略建议 */
    private String recommendation;

    /** 通知时间戳 */
    private LocalDateTime timestamp;
}