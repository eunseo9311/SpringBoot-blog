-- 초기 블로그 스키마 생성
-- User, Article, Comment 테이블 및 기본 제약조건 정의

-- 사용자 테이블 (예약어 user를 위해 따옴표 사용)
CREATE TABLE "user" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    nickname VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- 게시글 테이블
CREATE TABLE "article" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(500),
    content TEXT,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_article_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

-- 댓글 테이블  
CREATE TABLE comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(1000),
    user_id BIGINT,
    article_id BIGINT,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES "user"(id),
    CONSTRAINT fk_comment_article FOREIGN KEY (article_id) REFERENCES "article"(id)
);