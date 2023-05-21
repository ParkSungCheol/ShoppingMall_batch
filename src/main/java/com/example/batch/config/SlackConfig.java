//package com.example.batch.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import net.gpedro.integrations.slack.SlackApi;
//
//@Configuration
//public class SlackConfig {
//
//    @Value("${slack.webhook-uri}")
//    private String slackToken;
//
//    @Bean
//    public SlackApi slackApi(){
//        return new SlackApi(slackToken);
//    }
//}
