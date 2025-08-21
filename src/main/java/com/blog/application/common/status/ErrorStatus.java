package com.blog.application.common.status;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorStatus {
    
    // 공통 에러
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E000", "서버에서 오류가 발생했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "E001", "잘못된 요청입니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "E002", "입력값이 올바르지 않습니다."),
    
    // 인증/인가 에러
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "E100", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "E101", "접근 권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "E102", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "E103", "만료된 토큰입니다."),
    
    // 사용자 관련 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "E200", "존재하지 않는 사용자입니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "E201", "이미 존재하는 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "E202", "비밀번호가 일치하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "E203", "유효하지 않은 리프레시 토큰입니다."),
    
    // 게시글 관련 에러
    ARTICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "E300", "존재하지 않는 게시글입니다."),
    ARTICLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "E301", "게시글에 대한 권한이 없습니다."),
    ARTICLE_TITLE_EMPTY(HttpStatus.BAD_REQUEST, "E302", "게시글 제목이 비어있습니다."),
    ARTICLE_CONTENT_EMPTY(HttpStatus.BAD_REQUEST, "E303", "게시글 내용이 비어있습니다."),
    
    // 댓글 관련 에러
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "E400", "존재하지 않는 댓글입니다."),
    COMMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "E401", "댓글에 대한 권한이 없습니다."),
    COMMENT_CONTENT_EMPTY(HttpStatus.BAD_REQUEST, "E402", "댓글 내용이 비어있습니다.");
    
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}