package com.blog.application.config;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class TestContainerConfig {
    
    @Container
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);
    
    static {
        mysqlContainer.start();
        
        // 테스트 환경 설정을 위한 시스템 프로퍼티 설정
        System.setProperty("spring.datasource.url", mysqlContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", mysqlContainer.getUsername());
        System.setProperty("spring.datasource.password", mysqlContainer.getPassword());
        System.setProperty("spring.datasource.driver-class-name", "com.mysql.cj.jdbc.Driver");
        System.setProperty("spring.jpa.database-platform", "org.hibernate.dialect.MySQLDialect");
        System.setProperty("spring.jpa.hibernate.ddl-auto", "create-drop");
    }
}