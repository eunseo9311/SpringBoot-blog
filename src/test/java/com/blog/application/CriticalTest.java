package com.blog.application;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") 
class CriticalTest {

    @Test
    void contextLoads() {
        // Spring 컨텍스트가 올바르게 로드되는지 확인
    }
}