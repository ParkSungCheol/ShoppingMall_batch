package com.example.batch.config;

import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackConfig {

    @Value("${slack.webhook-uri}")
    private String slackToken;

    @Bean
    public SlackApi slackApi(){
        return new SlackApi(slackToken);
    }
    
    @Bean(name = "slackAttachment_completed")
    public SlackAttachment slackAttachment_completed(){
        SlackAttachment slackAttachment = new SlackAttachment();

        slackAttachment.setFallback("Complete");
        slackAttachment.setColor("good");
        slackAttachment.setTitle("Complete Directory");

        return slackAttachment;
    }

    @Bean(name = "slackAttachment_failed")
    public SlackAttachment slackAttachment_failed(){
        SlackAttachment slackAttachment = new SlackAttachment();

        slackAttachment.setFallback("Failed");
        slackAttachment.setColor("danger");
        slackAttachment.setTitle("Failed Directory");

        return slackAttachment;
    }

    @Bean(name = "slackMessage_completed")
    public SlackMessage slackMessage_completed(){
        SlackMessage slackMessage = new SlackMessage();

        slackMessage.setIcon(":white_check_mark:");
        slackMessage.setText("Complete Detected");
        slackMessage.setUsername("Complete Catcher");
        return slackMessage;
    }
    
    @Bean(name = "slackMessage_failed")
    public SlackMessage slackMessage_failed(){
        SlackMessage slackMessage = new SlackMessage();

        slackMessage.setIcon(":ghost:");
        slackMessage.setText("Error Detected");
        slackMessage.setUsername("Error Catcher");
        return slackMessage;
    }
}
