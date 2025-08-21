-- 성능 최적화를 위한 인덱스 추가
-- 자주 조회되는 컬럼과 외래키에 대한 인덱스 생성

-- 사용자 이메일 조회 최적화 (로그인 시 사용)
-- UNIQUE 제약조건이 있지만 명시적 인덱스 생성
CREATE INDEX idx_user_email ON "user"(email);

-- 게시글 조회 최적화
CREATE INDEX idx_article_user_id ON "article"(user_id);
CREATE INDEX idx_article_title ON "article"(title);

-- 댓글 조회 최적화  
CREATE INDEX idx_comment_article_id ON comment(article_id);
CREATE INDEX idx_comment_user_id ON comment(user_id);