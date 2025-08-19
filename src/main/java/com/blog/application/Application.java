package com.blog.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    // Spring Boot 애플리케이션의 진입점 (main 메서드)
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

// Application.java는 Spring Boot 애플리케이션의 시작점으로,
// 모든 스프링 컴포넌트를 스캔하고 애플리케이션 컨텍스트를 구성하는 역할을 한다