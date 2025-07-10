package com.wizard.business.component;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.taobao.api.ApiException;
import com.wizard.common.model.dto.DingDingMessageDTO;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * @author wizard
 * @date 2025年07月10日 15:41
 * @desc 推送通知组件
 */
@Component
public class PushMessage {

	public static final String CUSTOM_ROBOT_TOKEN = "61d0b64d2f3807c2a52d4c2dcc938d0d54d4719f8cc0683078c02b14bed1b25f";

	public static final String SECRET = "SECb7c748cdc3e5b554e42b9bb5705ec185f946921720239d84a253576a0e75ff54";

	public void pushMessage(DingDingMessageDTO dingDingMessageDTO) {
		try {
			Long timestamp = System.currentTimeMillis();
			System.out.println(timestamp);
			String secret = SECRET;
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
			OapiRobotSendResponse rsp = client.execute(req, CUSTOM_ROBOT_TOKEN);
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
