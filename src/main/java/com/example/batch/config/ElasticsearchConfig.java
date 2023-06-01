package com.example.batch.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

@Configuration
public class ElasticsearchConfig {
    @Bean
    public RestHighLevelClient client() {
    	return new RestHighLevelClient(
    		    RestClient.builder(
    		        // Elasticsearch 호스트 및 포트 설정
    		        new HttpHost("ec2-54-180-119-204.ap-northeast-2.compute.amazonaws.com", 3308, "http")
    		        // 추가적인 Elasticsearch 호스트나 포트가 있다면 여기에 추가
    		    )
    		);
    }
}

