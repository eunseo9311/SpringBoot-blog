package com.blog.application.repository;

import com.blog.application.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    
    Optional<RefreshToken> findByToken(String token);
    
    void deleteByEmail(String email);
}

//Redis 기반 리프레시 토큰 저장소 인터페이스
//토큰으로 검색, 이메일로 삭제 기능 제공