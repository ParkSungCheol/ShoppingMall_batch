package com.example.batch.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

@Configuration
@Profile("main")
public class ElasticsearchConfig {
	
	@Value("${elasticsearch.url}")
    private String url;
	
	@Value("${elasticsearch.port}")
    private String port;

    @Bean
    // NativeSearchQuery 을 이용해 요청보낼 때 사용
    public ElasticsearchOperations elasticsearchOperations() {
        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(url + ":" + port)
                .build();

        return new ElasticsearchRestTemplate(RestClients.create(clientConfiguration).rest());
    }
    
    @Bean
    // SearchSourceBuilder 을 이용해 요청보낼 때 사용
    public RestHighLevelClient client() {
    	return new RestHighLevelClient(
    		    RestClient.builder(
    		        // Elasticsearch 호스트 및 포트 설정
    		        new HttpHost(url, Integer.parseInt(port), "http")
    		        // 추가적인 Elasticsearch 호스트나 포트가 있다면 여기에 추가
    		    )
    		);
    }
}

