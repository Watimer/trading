package com.wizard.common.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.wizard.common.enums.NewsTypeEnum;
import com.wizard.common.model.vo.NewsTokenVO;
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
			"BITHUMB","COINBASE"
	);

	// 上架相关关键词
	private static final Set<String> LISTING_KEYWORDS = Set.of(
			"上线", "上市", "推出", "支持", "新增", "添加", "开放", "启动",
			"launch", "list", "support", "add", "introduce"
	);

	// 下架相关关键词
	private static final Set<String> DELISTING_KEYWORDS = Set.of(
			"下架", "下线", "停止", "移除", "删除", "暂停", "终止", "取消",
			"delist", "remove", "suspend", "terminate", "discontinue"
	);

	/**
	 * 从新闻文本中提取代币名称
	 */
	public static List<String> extractTokenNames(String newsText) {

		if (StrUtil.isBlank(newsText)) {
			return new ArrayList<>();
		}

		List<NewsTokenVO> newsTokenVOList = new ArrayList<>();

		List<String> tokens = new ArrayList<>();

		// 匹配上线相关语境中的代币
		String[] patterns = {
				"上线\\s*([A-Z]{2,10})(?:代币|币|token)?",
				"上市\\s*([A-Z]{2,10})(?:代币|币|token)?",
				"推出\\s*([A-Z]{2,10})(?:代币|币|token)?",
				"支持\\s*([A-Z]{2,10})(?:代币|币|token)?",
				"新增\\s*([A-Z]{2,10})(?:代币|币|token)?"
		};

		// 匹配上线的代币
		for (String patternStr : patterns) {
			Pattern pattern = Pattern.compile(patternStr);
			Matcher matcher = pattern.matcher(newsText);
			while (matcher.find()) {
				String token = matcher.group(1).toUpperCase();
				// 过滤交易所名称
				if (!EXCHANGE_KEYWORDS.contains(token) && !tokens.contains(token)) {
					NewsTokenVO newsTokenVO = NewsTokenVO.builder()
							.side("BUY")
							.symbol(token)
							.build();
					newsTokenVOList.add(newsTokenVO);
					tokens.add(token);
				}
			}
		}

		return tokens;
	}


	/**
	 * 基于下架语境提取代币
	 */
	private static List<String> extractByDelistingContext(String text) {
		List<String> tokens = new ArrayList<>();

		// 匹配下架相关语境中的代币
		String[] patterns = {
				"下架\\s*([A-Z]{2,10})(?:代币|币|token)?",
				"下线\\s*([A-Z]{2,10})(?:代币|币|token)?",
				"停止\\s*([A-Z]{2,10})(?:代币|币|token)?",
				"移除\\s*([A-Z]{2,10})(?:代币|币|token)?",
				"暂停\\s*([A-Z]{2,10})(?:代币|币|token)?",
				"delist\\s+([A-Z]{2,10})(?:\\s+token)?",
				"remove\\s+([A-Z]{2,10})(?:\\s+token)?"
		};

		for (String patternStr : patterns) {
			Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(text);
			while (matcher.find()) {
				String token = matcher.group(1).toUpperCase();
				if (!tokens.contains(token)) {
					tokens.add(token);
				}
			}
		}

		return tokens;
	}

	/**
	 * 基于上架语境提取代币
	 */
	private static List<String> extractByListingContext(String text) {
		List<String> tokens = new ArrayList<>();

		String[] patterns = {
				"上线\\s*([A-Z]{2,10})(?:代币|币|token)?",
				"上市\\s*([A-Z]{2,10})(?:代币|币|token)?",
				"推出\\s*([A-Z]{2,10})(?:代币|币|token)?",
				"支持\\s*([A-Z]{2,10})(?:代币|币|token)?",
				"新增\\s*([A-Z]{2,10})(?:代币|币|token)?",
				"launch\\s+([A-Z]{2,10})(?:\\s+token)?",
				"list\\s+([A-Z]{2,10})(?:\\s+token)?"
		};

		for (String patternStr : patterns) {
			Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(text);
			while (matcher.find()) {
				String token = matcher.group(1).toUpperCase();
				if (!tokens.contains(token)) {
					tokens.add(token);
				}
			}
		}

		return tokens;
	}

	/**
	 * 处理多个代币的情况
	 * 如：ALPHA、BSW等5种代币
	 */
	private static List<String> extractMultipleTokens(String text) {
		List<String> tokens = new ArrayList<>();

		// 匹配多个代币的模式：ALPHA、BSW等
		Pattern multiPattern = Pattern.compile("([A-Z]{2,10})(?:[、,]\\s*([A-Z]{2,10}))*(?:等\\d*种?代币)?");
		Matcher matcher = multiPattern.matcher(text);

		while (matcher.find()) {
			String fullMatch = matcher.group(0);

			// 检查是否在上架/下架语境中
			if (isInRelevantContext(text, matcher.start())) {
				// 提取所有代币名称
				Pattern tokenPattern = Pattern.compile("([A-Z]{2,10})");
				Matcher tokenMatcher = tokenPattern.matcher(fullMatch);

				while (tokenMatcher.find()) {
					String token = tokenMatcher.group(1).toUpperCase();
					if (!EXCHANGE_KEYWORDS.contains(token) && !tokens.contains(token)) {
						tokens.add(token);
					}
				}
			}
		}

		return tokens;
	}

	/**
	 * 检查是否在相关语境中（上架或下架）
	 */
	private static boolean isInRelevantContext(String text, int position) {
		int start = Math.max(0, position - 30);
		int end = Math.min(text.length(), position + 30);
		String context = text.substring(start, end).toLowerCase();

		boolean hasListingKeyword = LISTING_KEYWORDS.stream()
				.anyMatch(keyword -> context.contains(keyword.toLowerCase()));

		boolean hasDelistingKeyword = DELISTING_KEYWORDS.stream()
				.anyMatch(keyword -> context.contains(keyword.toLowerCase()));

		return hasListingKeyword || hasDelistingKeyword;
	}

	/**
	 * 过滤和去重
	 */
	private static List<String> filterAndDeduplicate(List<String> tokens) {
		return tokens.stream()
				.filter(token -> token.length() >= 2 && token.length() <= 10)
				.filter(token -> !EXCHANGE_KEYWORDS.contains(token))
				.distinct()
				.sorted()
				.toList();
	}


	/**
	 * 快速提取单个代币（适用于简单场景）
	 */
	public static String extractFirstToken(String newsText) {
		List<String> tokens = extractTokenNames(newsText);
		return tokens.isEmpty() ? null : tokens.get(0);
	}

	/**
	 * 从新闻文本中提取下架的代币名称
	 * @param newsText 新闻文本
	 * @return 下架的代币名称列表
	 */
	public static List<String> extractDelistedTokens(String newsText) {
		if (StrUtil.isBlank(newsText)) {
			return new ArrayList<>();
		}

		List<String> tokens = new ArrayList<>();

		try {
			// 方法1：精确匹配下架语境中的代币
			tokens.addAll(extractByDelistingContext(newsText));

			// 方法2：处理多个代币的情况（如：ALPHA、BSW等5种代币）
			tokens.addAll(extractMultipleTokens(newsText));

			// 去重并过滤
			return filterAndDeduplicate(tokens);

		} catch (Exception e) {
			log.error("提取下架代币名称时发生错误: {}", e.getMessage(), e);
			return new ArrayList<>();
		}
	}

	/**
	 * 从新闻文本中提取上架的代币名称
	 * @param newsText 新闻文本
	 * @return 上架的代币名称列表
	 */
	public static List<String> extractListedTokens(String newsText) {
		if (StrUtil.isBlank(newsText)) {
			return new ArrayList<>();
		}

		List<String> tokens = new ArrayList<>();

		try {
			// 匹配上架语境中的代币
			tokens.addAll(extractByListingContext(newsText));
			tokens.addAll(extractMultipleTokens(newsText));

			return filterAndDeduplicate(tokens);

		} catch (Exception e) {
			log.error("提取上架代币名称时发生错误: {}", e.getMessage(), e);
			return new ArrayList<>();
		}
	}


	/**
	 * 分析新闻类型（上架/下架）
	 */
	public static NewsTypeEnum analyzeNewsType(String newsText) {
		if (StrUtil.isBlank(newsText)) {
			return NewsTypeEnum.UNKNOWN;
		}

		String lowerText = newsText.toLowerCase();

		boolean hasListingKeyword = LISTING_KEYWORDS.stream()
				.anyMatch(keyword -> lowerText.contains(keyword.toLowerCase()));

		boolean hasDelistingKeyword = DELISTING_KEYWORDS.stream()
				.anyMatch(keyword -> lowerText.contains(keyword.toLowerCase()));

		// 下架
		if (hasDelistingKeyword) {
			return NewsTypeEnum.DELISTING;
		// 上架
		} else if (hasListingKeyword) {
			return NewsTypeEnum.LISTING;
		// 未知
		} else {
			return NewsTypeEnum.UNKNOWN;
		}
	}

	/**
	 * 综合分析新闻并返回结果
	 */
	public static TokenNewsResult analyzeTokenNews(String newsText) {
		NewsTypeEnum newsType = analyzeNewsType(newsText);
		List<String> tokens = new ArrayList<>();

		switch (newsType) {
			case LISTING:
				tokens = extractListedTokens(newsText);
				break;
			case DELISTING:
				tokens = extractDelistedTokens(newsText);
				break;
			default:
				// 尝试通用提取
				tokens.addAll(extractListedTokens(newsText));
				tokens.addAll(extractDelistedTokens(newsText));
				tokens = filterAndDeduplicate(tokens);
				break;
		}

		return TokenNewsResult.builder()
				.originalText(newsText)
				.newsType(newsType)
				.extractedTokens(tokens)
				.tokenCount(tokens.size())
				.build();
	}


	/**
	 * 代币新闻分析结果
	 */
	public static class TokenNewsResult {
		private String originalText;
		private NewsTypeEnum newsType;
		private List<String> extractedTokens;
		private int tokenCount;

		public static TokenNewsResultBuilder builder() {
			return new TokenNewsResultBuilder();
		}

		// Getters
		public String getOriginalText() { return originalText; }
		public NewsTypeEnum getNewsType() { return newsType; }
		public List<String> getExtractedTokens() { return extractedTokens; }
		public int getTokenCount() { return tokenCount; }

		public static class TokenNewsResultBuilder {
			private String originalText;
			private NewsTypeEnum newsType;
			private List<String> extractedTokens;
			private int tokenCount;

			public TokenNewsResultBuilder originalText(String originalText) {
				this.originalText = originalText;
				return this;
			}

			public TokenNewsResultBuilder newsType(NewsTypeEnum newsType) {
				this.newsType = newsType;
				return this;
			}

			public TokenNewsResultBuilder extractedTokens(List<String> extractedTokens) {
				this.extractedTokens = extractedTokens;
				return this;
			}

			public TokenNewsResultBuilder tokenCount(int tokenCount) {
				this.tokenCount = tokenCount;
				return this;
			}

			public TokenNewsResult build() {
				TokenNewsResult result = new TokenNewsResult();
				result.originalText = this.originalText;
				result.newsType = this.newsType;
				result.extractedTokens = this.extractedTokens;
				result.tokenCount = this.tokenCount;
				return result;
			}
		}

		@Override
		public String toString() {
			return String.format("TokenNewsResult{type=%s, tokens=%s, count=%d}",
					newsType.getDescription(), extractedTokens, tokenCount);
		}
	}

	public static void main(String[] args) {
		String[] testCases = {
				"Upbit将在KRW、BTC、USDT市场上线ERA代币"
		};

		for (String news : testCases) {
			TokenNewsResult result = analyzeTokenNews(news);
			System.out.println("新闻: " + news);
			System.out.println("类型: " + result.getNewsType().getDescription());
			System.out.println("代币: " + result.getExtractedTokens());
			System.out.println("---");
		}
	}
}
