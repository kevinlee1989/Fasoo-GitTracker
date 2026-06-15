package com.example.demo.gitlab;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GitlabConfig {

    @Bean
    public RestClient gitlabRestClient(
            @Value("${gitlab.base-url}") String baseUrl,
            @Value("${gitlab.token}") String token
    ) {
        // RestClient를 객체를 생성하여 반환
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("PRIVATE-TOKEN", token)
                .build();
    }
}