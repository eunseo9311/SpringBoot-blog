-- 좋아요/북마크 기능을 위한 테이블 추가
-- 레이스 컨디션 방지를 위한 UNIQUE 제약조건 포함

-- 게시글 좋아요 테이블
CREATE TABLE article_like (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    article_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_like_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT fk_like_article FOREIGN KEY (article_id) REFERENCES "article"(id) ON DELETE CASCADE,
    CONSTRAINT uk_article_like UNIQUE (user_id, article_id)
);

-- 게시글 북마크 테이블
CREATE TABLE article_bookmark (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    article_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bookmark_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT fk_bookmark_article FOREIGN KEY (article_id) REFERENCES "article"(id) ON DELETE CASCADE,
    CONSTRAINT uk_article_bookmark UNIQUE (user_id, article_id)
);

-- 게시글 테이블에 좋아요 개수 컬럼 추가
ALTER TABLE "article" ADD COLUMN like_count BIGINT DEFAULT 0 NOT NULL;

-- 성능을 위한 인덱스 추가
CREATE INDEX idx_article_like_user_id ON article_like(user_id);
CREATE INDEX idx_article_like_article_id ON article_like(article_id);
CREATE INDEX idx_article_bookmark_user_id ON article_bookmark(user_id);
CREATE INDEX idx_article_bookmark_article_id ON article_bookmark(article_id);