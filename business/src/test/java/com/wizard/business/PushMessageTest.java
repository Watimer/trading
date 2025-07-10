package com.wizard.business;

import com.wizard.business.component.PushMessage;
import com.wizard.business.service.TradingViewService;
import com.wizard.common.model.dto.DingDingMessageDTO;
import com.wizard.common.model.dto.MarkdownDTO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author wizard
 * @date 2025年07月10日 15:58
 * @desc
 */
@SpringBootTest
public class PushMessageTest {

	@Resource
	PushMessage pushMessage;

	@Resource
	TradingViewService tradingViewService;

	@Test
	public void testPushMessage(){
		MarkdownDTO markdownDTO = MarkdownDTO.builder()
				.title("测试MarkDown消息")
				.text("""
| ID | 用户名 | 状态 |
|:---:|---|---|
| 1 | 张三 | 活跃 |
| 2 | 李四 | 休眠 |
| 3 | 王五 | 活跃 |""")
				.build();
		DingDingMessageDTO dingDingMessageDTO = DingDingMessageDTO.builder()
				.msgtype("markdown")
				.markdown(markdownDTO)
				.build();
		pushMessage.pushMessage(dingDingMessageDTO);
	}

	@Test
	public void testScan(){
		tradingViewService.scan();
	}
}
