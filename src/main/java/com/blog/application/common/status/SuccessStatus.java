package com.blog.application.common.status;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessStatus {
    
    // 인증 관련
    SIGNUP_SUCCESS(HttpStatus.CREATED, "A001", "회원가입이 성공적으로 완료되었습니다."),
    LOGIN_SUCCESS(HttpStatus.OK, "A002", "로그인이 성공적으로 완료되었습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "A003", "로그아웃이 성공적으로 완료되었습니다."),
    TOKEN_REFRESH_SUCCESS(HttpStatus.OK, "A004", "토큰이 성공적으로 갱신되었습니다."),
    
    // 게시글 관련
    ARTICLE_CREATE_SUCCESS(HttpStatus.CREATED, "B001", "게시글이 성공적으로 작성되었습니다."),
    ARTICLE_READ_SUCCESS(HttpStatus.OK, "B002", "게시글을 성공적으로 조회했습니다."),
    ARTICLE_UPDATE_SUCCESS(HttpStatus.OK, "B003", "게시글이 성공적으로 수정되었습니다."),
    ARTICLE_DELETE_SUCCESS(HttpStatus.OK, "B004", "게시글이 성공적으로 삭제되었습니다."),
    
    // 댓글 관련
    COMMENT_CREATE_SUCCESS(HttpStatus.CREATED, "C001", "댓글이 성공적으로 작성되었습니다."),
    COMMENT_READ_SUCCESS(HttpStatus.OK, "C002", "댓글을 성공적으로 조회했습니다."),
    COMMENT_UPDATE_SUCCESS(HttpStatus.OK, "C003", "댓글이 성공적으로 수정되었습니다."),
    COMMENT_DELETE_SUCCESS(HttpStatus.OK, "C004", "댓글이 성공적으로 삭제되었습니다."),
    
    // 사용자 관련
    USER_READ_SUCCESS(HttpStatus.OK, "U001", "사용자 정보를 성공적으로 조회했습니다."),
    USER_LIST_SUCCESS(HttpStatus.OK, "U002", "사용자 목록을 성공적으로 조회했습니다.");
    
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}