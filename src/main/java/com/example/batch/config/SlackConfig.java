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

    @Bean
    public SlackAttachment slackAttachment(){
        SlackAttachment slackAttachment = new SlackAttachment();

        slackAttachment.setFallback("Error");
        slackAttachment.setColor("danger");
        slackAttachment.setTitle("Error Directory");

        return slackAttachment;
    }

    @Bean
    public SlackMessage slackMessage(){
        SlackMessage slackMessage = new SlackMessage();

        slackMessage.setIcon(":ghost:");
        slackMessage.setText("Error Detected");
        slackMessage.setUsername("Error Catcher");
        return slackMessage;
    }

}
