package com.wizard.common.utils;

import com.wizard.common.enums.LinePatternEnum;
import lombok.Getter; // 确保项目中已引入 Lombok 依赖
import java.util.*;

public class KLinePatternChecker {

	// K线数据结构 (K-Line Data Structure)
	public static class KLine {
		double open, high, low, close;
		LinePatternEnum expectedType; // 2. 类型已更新为 LinePatternEnum

		public KLine(double open, double high, double low, double close, LinePatternEnum expectedType) {
			this.high = Math.max(high, Math.max(open, close));
			this.low = Math.min(low, Math.min(open, close));
			this.open = open;
			this.close = close;
			this.expectedType = expectedType; // 2. 类型已更新
		}

		@Override
		public String toString() {
			return String.format("[%.5f, %.5f, %.5f, %.5f]", open, high, low, close);
		}
	}

	// region: --- 终极版参数配置 (Ultimate Parameter Configuration) ---
	private static final double DOJI_BODY_TO_RANGE_RATIO = 0.22;
	private static final double DOJI_BODY_TO_SHADOWS_RATIO = 0.28;
	private static final double HAMMER_BODY_LOCATION_THRESHOLD_MIN = 0.60;
	private static final double INV_HAMMER_BODY_LOCATION_THRESHOLD_MAX = 0.40;
	private static final double MAIN_SHADOW_TO_BODY_MIN_RATIO = 1.35;
	private static final double OTHER_SHADOW_TO_BODY_MAX_RATIO = 1.6;
	private static final double MAIN_TO_OTHER_SHADOW_MIN_RATIO = 2.1;
	private static final double MIN_BODY_SIZE = 1e-7;
	// endregion

	// 3. 主检测方法的返回类型已更新
	public static LinePatternEnum detectKLineType(double open, double high, double low, double close) {
		double body = Math.abs(close - open);
		double range = high - low;

		if (range < 1e-7) return LinePatternEnum.UNKNOW; // 3. 更新为 UNKNOW

		double upperShadow = high - Math.max(open, close);
		double lowerShadow = Math.min(open, close) - low;

		double bodyCenter = (Math.max(open, close) + Math.min(open, close)) / 2.0;
		double bodyLocation = (bodyCenter - low) / range;

		// 3. 判断逻辑中的返回类型已全部更新
		if (isPingBar(body, upperShadow, lowerShadow, bodyLocation)) {
			return LinePatternEnum.PING_BAR;
		}
		if (isReversePingBar(body, upperShadow, lowerShadow, bodyLocation)) {
			return LinePatternEnum.REVERSE_PING_BAR;
		}
		if (isCrossCurve(body, upperShadow, lowerShadow, range)) {
			return LinePatternEnum.CROSS_CURVE;
		}

		return LinePatternEnum.UNKNOW;
	}

	// region: --- 终极版形态判断逻辑 (Ultimate Pattern Recognition Logic) ---

	// 4. 判断方法名和注释已更新，以匹配新的枚举
	private static boolean isCrossCurve(double body, double upperShadow, double lowerShadow, double range) {
		double totalShadows = upperShadow + lowerShadow;
		boolean isClassicDoji = body / range < DOJI_BODY_TO_RANGE_RATIO;
		boolean isLongWickDoji = totalShadows > 0 && (body / totalShadows < DOJI_BODY_TO_SHADOWS_RATIO);
		return isClassicDoji || isLongWickDoji;
	}

	private static boolean isPingBar(double body, double upperShadow, double lowerShadow, double bodyLocation) {
		if (bodyLocation < HAMMER_BODY_LOCATION_THRESHOLD_MIN) {
			return false;
		}
		if (body < MIN_BODY_SIZE) return false;

		boolean isLowerDominate = lowerShadow / (upperShadow + 1e-9) > MAIN_TO_OTHER_SHADOW_MIN_RATIO;

		return lowerShadow >= MAIN_SHADOW_TO_BODY_MIN_RATIO * body &&
				upperShadow <= OTHER_SHADOW_TO_BODY_MAX_RATIO * body &&
				isLowerDominate;
	}

	private static boolean isReversePingBar(double body, double upperShadow, double lowerShadow, double bodyLocation) {
		if (bodyLocation > INV_HAMMER_BODY_LOCATION_THRESHOLD_MAX) {
			return false;
		}
		if (body < MIN_BODY_SIZE) return false;

		boolean isUpperDominate = upperShadow / (lowerShadow + 1e-9) > MAIN_TO_OTHER_SHADOW_MIN_RATIO;

		return upperShadow >= MAIN_SHADOW_TO_BODY_MIN_RATIO * body &&
				lowerShadow <= OTHER_SHADOW_TO_BODY_MAX_RATIO * body &&
				isUpperDominate;
	}
	// endregion

	// 5. 测试数据中的期望类型已全部更新
	public static List<KLine> getTestKLines() {
		return List.of(
				// CROSS_CURVE (Doji)
				new KLine(0.19188, 0.19562, 0.18632, 0.19259, LinePatternEnum.CROSS_CURVE),
				new KLine(0.17936, 0.18626, 0.17646, 0.17946, LinePatternEnum.CROSS_CURVE),
				new KLine(0.17927, 0.18280, 0.17671, 0.17933, LinePatternEnum.CROSS_CURVE),
				new KLine(0.17166, 0.17410, 0.16945, 0.17210, LinePatternEnum.CROSS_CURVE),
				new KLine(0.17727, 0.17968, 0.17541, 0.17674, LinePatternEnum.CROSS_CURVE),
				new KLine(261.72, 262.97, 258.38, 261.49, LinePatternEnum.CROSS_CURVE),
				new KLine(302.60, 306.59, 295.64, 301.47, LinePatternEnum.CROSS_CURVE),
				new KLine(272.35, 274.65, 269.73, 271.74, LinePatternEnum.CROSS_CURVE),
				new KLine(267.77, 269.38, 265.73, 267.36, LinePatternEnum.CROSS_CURVE),
				new KLine(253.52, 254.36, 251.71, 252.97, LinePatternEnum.CROSS_CURVE),

				// PING_BAR (Hammer)
				new KLine(272.86, 275.05, 266.29, 274.82, LinePatternEnum.PING_BAR),
				new KLine(248.69, 249.09, 245.16, 248.00, LinePatternEnum.PING_BAR),
				new KLine(263.76, 265.19, 259.08, 264.72, LinePatternEnum.PING_BAR),
				new KLine(258.28, 260.24, 255.88, 259.14, LinePatternEnum.PING_BAR),
				new KLine(308.16, 309.96, 304.11, 309.42, LinePatternEnum.PING_BAR),
				new KLine(163.42, 163.77, 161.96, 163.16, LinePatternEnum.PING_BAR),
				new KLine(147.50, 147.59, 146.74, 147.26, LinePatternEnum.PING_BAR),
				new KLine(147.81, 147.94, 146.66, 147.88, LinePatternEnum.PING_BAR),
				new KLine(660.97, 660.98, 657.54, 660.52, LinePatternEnum.PING_BAR),
				new KLine(662.55, 662.89, 658.41, 661.24, LinePatternEnum.PING_BAR),

				// REVERSE_PING_BAR (Inverted Hammer)
				new KLine(308.26, 325.43, 307.02, 315.53, LinePatternEnum.REVERSE_PING_BAR),
				new KLine(259.32, 264.09, 257.36, 257.54, LinePatternEnum.REVERSE_PING_BAR),
				new KLine(263.95, 270.87, 262.24, 265.69, LinePatternEnum.REVERSE_PING_BAR),
				new KLine(255.93, 259.57, 254.90, 256.33, LinePatternEnum.REVERSE_PING_BAR),
				new KLine(301.96, 306.79, 301.55, 303.43, LinePatternEnum.REVERSE_PING_BAR),
				new KLine(128.51, 131.42, 128.21, 129.02, LinePatternEnum.REVERSE_PING_BAR),
				new KLine(141.72, 143.31, 141.56, 141.56, LinePatternEnum.REVERSE_PING_BAR),
				new KLine(151.21, 154.00, 151.17, 151.80, LinePatternEnum.REVERSE_PING_BAR),
				new KLine(661.78, 664.60, 661.44, 662.56, LinePatternEnum.REVERSE_PING_BAR),
				new KLine(665.93, 673.76, 665.79, 667.82, LinePatternEnum.REVERSE_PING_BAR)
		);
	}

	public static void main(String[] args) {
		int pass = 0, fail = 0;
		List<KLine> testKLines = getTestKLines();
		for (KLine k : testKLines) {
			LinePatternEnum result = detectKLineType(k.open, k.high, k.low, k.close); // 6. 更新变量类型
			boolean isCorrect = result == k.expectedType;
			if (isCorrect) pass++; else fail++;

			System.out.printf("K线 %s -> 检测结果: %-17s | 预期: %-17s [%s]%n",
					k, result, k.expectedType, isCorrect ? "✔️ 正确" : "❌ 错误");
		}
		System.out.printf("%n测试完成，总计 %d 条，通过 %d 条，失败 %d 条%n", testKLines.size(), pass, fail);
	}
}
