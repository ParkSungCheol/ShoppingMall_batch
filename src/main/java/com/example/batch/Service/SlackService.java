package com.example.batch.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.springframework.stereotype.Component;
import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackField;
import net.gpedro.integrations.slack.SlackMessage;

@Component
public class SlackService {
	
    private SlackApi slackApi;
    private SlackAttachment slackAttachment;
    private SlackMessage slackMessage;
	
	public SlackService(SlackApi slackApi) {
		this.slackApi = slackApi;
		slackAttachment = new SlackAttachment();
		slackMessage = new SlackMessage();
	}
    
    public void call(int flag, String msg) {
    	// Failed
    	if(flag == 0) {
    		slackAttachment.setFallback("Failed");
            slackAttachment.setColor("danger");
            slackAttachment.setTitle("Failed Directory");
            
            slackMessage.setIcon(":ghost:");
            slackMessage.setText("Error Detected");
            slackMessage.setUsername("Error Catcher");
    	}
    	// Completed    	
    	else {
    		slackAttachment.setFallback("Complete");
            slackAttachment.setColor("good");
            slackAttachment.setTitle("Complete Directory");
            
            slackMessage.setIcon(":white_check_mark:");
            slackMessage.setText("Complete Detected");
            slackMessage.setUsername("Complete Catcher");
    	}
    	
    	slackAttachment.setText(msg);

        // 현재 날짜와 시간 가져오기
        Date currentDate = new Date();
        // 대한민국 표준시(KST)로 변환하기
        TimeZone kstTimeZone = TimeZone.getTimeZone("Asia/Seoul");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(kstTimeZone);
        String kstDateTime = dateFormat.format(currentDate);
        slackAttachment.setFields(
                List.of(
                        new SlackField().setTitle("Request Time").setValue(kstDateTime)
                )
        );

        slackMessage.setAttachments(Collections.singletonList(slackAttachment));

        slackApi.call(slackMessage);
    }
}
