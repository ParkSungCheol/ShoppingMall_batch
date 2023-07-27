package com.example.batch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import net.gpedro.integrations.slack.SlackApi;

@Configuration
@Profile("main")
public class SlackConfig {

    @Value("${slack.webhook-uri}")
    private String slackToken;

    @Bean
    public SlackApi slackApi(){
        return new SlackApi(slackToken);
    }
}
