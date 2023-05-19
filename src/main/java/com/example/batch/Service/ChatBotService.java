package com.example.batch.Service;

import org.springframework.stereotype.Component;

import com.slack.api.Slack;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;

@Component
public class ChatBotService {
	private static Slack slack = Slack.getInstance();
	private static String token = "xoxb-5292419668372-5286999844805-JoGDDwkjg6MSl6L8Dqop48ZJ";
	
	public void sendSlack(String msg) throws Exception  {
		ChatPostMessageResponse response = slack.methods(token).chatPostMessage(req -> req
				.channel("C0597JZ9GN4") // Channel ID
				.text(msg));
	}
}
