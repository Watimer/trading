package com.wizard.common.model;

/**
 * 趋势方向枚举
 */
public enum TrendDirection {
    /** 上升趋势 */
    UP("↑", "上升"),

    /** 下降趋势 */
    DOWN("↓", "下降"),

    /** 趋势不明确或震荡 */
    UNCLEAR("/", "不明确");

    private final String symbol;
    private final String description;

    TrendDirection(String symbol, String description) {
        this.symbol = symbol;
        this.description = description;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDescription() {
        return description;
    }
}