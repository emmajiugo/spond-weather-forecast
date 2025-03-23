package me.emmajiugo.spond.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.concurrent.TimeUnit;

@Configuration
public class AppConfig {

    @Value("${weather.cache.maxSize:100}")
    private int cacheMaxSize;

    @Value("${weather.cache.ttl:60}")
    private int cacheTtlMinutes;

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                //.defaultHeader("User-Agent", "Spond/1.0")
                .build();
    }

    // IMPROVEMENT:
    // Use Redis or other distributed cache for better scalability
    @Bean
    public Cache<String, JsonNode> weatherCacheManager() {
        return Caffeine.newBuilder()
                .expireAfterWrite(cacheTtlMinutes, TimeUnit.MINUTES)
                .maximumSize(cacheMaxSize)
                .build();
    }
}
