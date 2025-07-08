package com.wizard.common.model;

/**
 * 信号级别枚举
 */
public enum SignalLevel {
    /** 强烈级别 - 多周期方向一致，建议立即执行交易 */
    STRONG("🔵", "强烈", "多周期方向一致，建议立即执行交易"),

    /** 中等级别 - 部分背离，需等待确认或控制仓位 */
    MEDIUM("🟡", "中等", "部分背离，需等待确认或控制仓位"),

    /** 较弱级别 - 潜在反转信号，风险较高，轻仓尝试 */
    WEAK("🟠", "较弱", "潜在反转信号，风险较高，轻仓尝试"),

    /** 观望级别 - 多周期冲突，不建议入场 */
    WAIT("⚪", "观望", "多周期冲突，不建议入场");

    private final String icon;
    private final String name;
    private final String description;

    SignalLevel(String icon, String name, String description) {
        this.icon = icon;
        this.name = name;
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}