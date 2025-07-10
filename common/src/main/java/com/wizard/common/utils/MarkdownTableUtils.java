package com.wizard.common.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Markdown 表格转换工具类
 */
public class MarkdownTableUtils {

	/**
	 * 将二维数据列表转换为Markdown表格
	 * @param headers 表头数组
	 * @param data 表格数据(二维列表)
	 * @param alignment 对齐方式(可选，如：["left", "center", "right"])
	 * @return Markdown格式的表格字符串
	 */
	public static String generateMarkdownTable(String[] headers, List<String[]> data, String[] alignment) {
		if (headers == null || headers.length == 0) {
			throw new IllegalArgumentException("Headers cannot be null or empty");
		}

		StringBuilder markdown = new StringBuilder();

		// 1. 构建表头
		markdown.append(buildTableRow(headers));

		// 2. 构建分隔线
		markdown.append(buildSeparatorLine(headers.length, alignment));

		// 3. 构建数据行
		for (String[] row : data) {
			if (row.length != headers.length) {
				throw new IllegalArgumentException("Row data length does not match headers length");
			}
			markdown.append(buildTableRow(row));
		}

		return markdown.toString();
	}

	private static String buildTableRow(String[] cells) {
		StringBuilder row = new StringBuilder("|");
		for (String cell : cells) {
			row.append(" ").append(escapeCell(cell)).append(" |");
		}
		return row.append("\n").toString();
	}

	private static String buildSeparatorLine(int columnCount, String[] alignment) {
		StringBuilder line = new StringBuilder("|");
		for (int i = 0; i < columnCount; i++) {
			String align = (alignment != null && i < alignment.length) ? alignment[i] : "left";
			switch (align.toLowerCase()) {
				case "center":
					line.append(":---:");
					break;
				case "right":
					line.append("---:");
					break;
				default: // left
					line.append("---");
					break;
			}
			line.append("|");
		}
		return line.append("\n").toString();
	}

	private static String escapeCell(String content) {
		if (content == null) {
			return "";
		}
		return content.replace("|", "\\|");
	}

	/**
	 * 将Markdown表格转换为HTML表格
	 * @param markdownTable Markdown表格文本
	 * @return HTML表格字符串
	 */
	public static String markdownToHtml(String markdownTable) {
		if (markdownTable == null || markdownTable.trim().isEmpty()) {
			return "";
		}

		String[] lines = markdownTable.split("\n");
		if (lines.length < 2) {
			throw new IllegalArgumentException("Invalid markdown table format");
		}

		StringBuilder html = new StringBuilder("<table>\n");

		// 处理表头
		html.append("  <thead>\n    <tr>\n");
		String[] headerCells = parseMarkdownRow(lines[0]);
		for (String cell : headerCells) {
			html.append("      <th>").append(cell).append("</th>\n");
		}
		html.append("    </tr>\n  </thead>\n");

		// 处理表格数据
		html.append("  <tbody>\n");
		for (int i = 2; i < lines.length; i++) {
			String[] rowCells = parseMarkdownRow(lines[i]);
			html.append("    <tr>\n");
			for (String cell : rowCells) {
				html.append("      <td>").append(cell).append("</td>\n");
			}
			html.append("    </tr>\n");
		}
		html.append("  </tbody>\n");

		return html.append("</table>").toString();
	}

	private static String[] parseMarkdownRow(String row) {
		if (!row.startsWith("|") || !row.endsWith("|")) {
			throw new IllegalArgumentException("Invalid markdown row format: " + row);
		}
		String content = row.substring(1, row.length() - 1);
		String[] cells = content.split("\\|");
		for (int i = 0; i < cells.length; i++) {
			cells[i] = cells[i].trim();
		}
		return cells;
	}

	public static void main(String[] args) {
		// 示例用法
		String[] headers = {"ID", "用户名", "状态"};
		List<String[]> data = new ArrayList<>();
		data.add(new String[]{"1", "张三", "活跃"});
		data.add(new String[]{"2", "李四", "休眠"});
		data.add(new String[]{"3", "王五", "活跃"});

		// 生成Markdown表格
		String markdown = generateMarkdownTable(headers, data, new String[]{"center", "left", "left", "center"});
		System.out.println("Markdown表格:\n" + markdown);

		// 转换为HTML
		String html = markdownToHtml(markdown);
		System.out.println("\nHTML表格:\n" + html);
	}
}
