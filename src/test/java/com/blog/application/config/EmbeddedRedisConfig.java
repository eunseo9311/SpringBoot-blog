package com.blog.application.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@TestConfiguration
public class EmbeddedRedisConfig {

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() throws IOException {
        int port = findAvailablePort();
        redisServer = RedisServer.builder()
                .port(port)
                .setting("maxmemory 128M")
                .build();
        redisServer.start();
        
        // 테스트 환경에서 Redis 포트 설정
        System.setProperty("spring.redis.port", String.valueOf(port));
        System.setProperty("spring.redis.host", "localhost");
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null && redisServer.isActive()) {
            redisServer.stop();
        }
    }

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        int port = Integer.parseInt(System.getProperty("spring.redis.port", "6379"));
        return new LettuceConnectionFactory("localhost", port);
    }

    private int findAvailablePort() throws IOException {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}