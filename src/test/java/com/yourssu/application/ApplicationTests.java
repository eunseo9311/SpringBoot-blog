package com.yourssu.application;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationTests {
    @Test
    void contextLoads() {
    }
}

// 전체 테스트 코드의 목적 및 메인 코드와의 연관성
// 검증 목표:
//
// Controller 테스트: HTTP 요청/응답을 시뮬레이션하여,
// REST API 엔드포인트가 올바른 상태 코드와 응답 데이터를 반환하는지 확인한다

//Repository 테스트: 데이터베이스에 엔티티가 올바르게 저장, 조회, 삭제되는지,
// 그리고 커스텀 메서드(예: findAllByUserId, deleteAllByArticleId 등)가 정상 동작하는지 검증한다

//Service 테스트: 비즈니스 로직(예: 게시글/댓글 저장, 수정, 삭제, 사용자 인증, 암호화 등)이 의도대로 수행되고,
// Repository와 적절하게 연동되는지를 단위 테스트와 모의 객체(Mocking)를 통해 확인한다

//애플리케이션 컨텍스트 테스트: 전체 스프링 부트 애플리케이션이 정상적으로 실행될 수 있는지 기본적인 설정을 검증한다

//메인 코드와의 연관성:
//
//테스트 코드들은 메인 애플리케이션의 Controller, Service, Repository 계층의 기능과 데이터 흐름을 확인한다
//이를 통해, 실제 운영 환경에서 API 호출 시 데이터가 올바르게 처리되고, 비즈니스 로직 및 데이터베이스 연동이 기대한 대로 동작함을 보증한다.
//또한, 변경 시 기존 기능에 대한 회귀 테스트(regression test) 역할을 하여, 유지보수 시 안정성을 제공한다