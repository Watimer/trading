package com.wizard.business.component;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.taobao.api.ApiException;
import com.wizard.common.model.dto.DingDingMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author wizard
 * @date 2025年07月10日 15:41
 * @desc 推送通知组件
 */
@Slf4j
@Component
public class PushMessage {

	public void pushText(String content) {
		DingDingMessageDTO dingDingMessageDTO = DingDingMessageDTO.builder()
				.msgtype("text")
				.context(content)
				.build();
		pushMessage(dingDingMessageDTO);
	}

	public void pushMessage(DingDingMessageDTO dingDingMessageDTO) {
		try {
			Long timestamp = System.currentTimeMillis();
			String secret = System.getenv("DINGDING_SECRET");
			String stringToSign = timestamp + "\n" + secret;
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
			byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
			String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)),"UTF-8");

			//sign字段和timestamp字段必须拼接到请求URL上，否则会出现 310000 的错误信息
			DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/robot/send?sign="+sign+"&timestamp="+timestamp);
			OapiRobotSendRequest req = new OapiRobotSendRequest();
			//设置消息类型
			req.setMsgtype(dingDingMessageDTO.getMsgtype());
			if("markdown".equals(req.getMsgtype())){
				OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
				markdown.setTitle(dingDingMessageDTO.getMarkdown().getTitle());
				markdown.setText(dingDingMessageDTO.getMarkdown().getText());
				req.setMarkdown(markdown);
			} else if("text".equals(req.getMsgtype())){
				OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
				text.setContent(dingDingMessageDTO.getContext());
				req.setText(text);
			}
			OapiRobotSendResponse rsp = client.execute(req, System.getenv("DINGDING_CUSTOM_ROBOT_TOKEN"));
		} catch (ApiException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		}
	}
}
