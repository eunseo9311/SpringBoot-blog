package com.blog.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.blog.application.repository.jpa")
//@EnableRedisRepositories(basePackages = "com.blog.application.repository.redis")  // Redis 사용하지 않으므로 비활성화
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

// Application.java는 Spring Boot 애플리케이션의 시작점으로,
// 모든 스프링 컴포넌트를 스캔하고 애플리케이션 컨텍스트를 구성하는 역할을 한다