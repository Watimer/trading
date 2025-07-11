package com.wizard.common.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wizard
 * @date 2025年07月11日 15:36
 * @desc
 */
@Slf4j
public class TokenNewsAnalyzerUtil {

	// 交易所关键词（需要过滤的）
	private static final Set<String> EXCHANGE_KEYWORDS = Set.of(
			"BITHUMB", "BINANCE", "OKX", "HUOBI", "COINBASE", "KRAKEN"
	);

	// 上线相关关键词
	private static final Set<String> LISTING_KEYWORDS = Set.of(
			"上线", "上市", "推出", "支持", "新增"
	);

	/**
	 * 从新闻文本中提取代币名称
	 */
	public static List<String> extractTokenNames(String newsText) {
		if (StrUtil.isBlank(newsText)) {
			return new ArrayList<>();
		}

		List<String> tokens = new ArrayList<>();

		// 匹配上线相关语境中的代币
		String[] patterns = {
				"上线\\s*([A-Z]{2,10})(?:代币|币|token)?",
				"上市\\s*([A-Z]{2,10})(?:代币|币|token)?",
				"推出\\s*([A-Z]{2,10})(?:代币|币|token)?",
				"支持\\s*([A-Z]{2,10})(?:代币|币|token)?",
				"新增\\s*([A-Z]{2,10})(?:代币|币|token)?"
		};

		for (String patternStr : patterns) {
			Pattern pattern = Pattern.compile(patternStr);
			Matcher matcher = pattern.matcher(newsText);
			while (matcher.find()) {
				String token = matcher.group(1).toUpperCase();
				// 过滤交易所名称
				if (!EXCHANGE_KEYWORDS.contains(token) && !tokens.contains(token)) {
					tokens.add(token);
				}
			}
		}

		return tokens;
	}

	/**
	 * 快速提取单个代币（适用于简单场景）
	 */
	public static String extractFirstToken(String newsText) {
		List<String> tokens = extractTokenNames(newsText);
		return tokens.isEmpty() ? null : tokens.get(0);
	}

	public static void main(String[] args) {
		String newsText = "Bithumb将在韩元市场上线RESOLV、BTC代币";
		List<String> list=  extractTokenNames(newsText);
		log.info("解析结果:{}",JSONUtil.toJsonStr(list));
	}
}
