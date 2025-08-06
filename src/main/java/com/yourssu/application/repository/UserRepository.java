package com.yourssu.application.repository;

import com.yourssu.application.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 이메일로 사용자 조회 (회원가입, 로그인, 탈퇴 등에 활용)
    Optional<User> findByEmail(String email);
}

//UserRepository는 사용자 엔티티에 대한 CRUD 작업과 이메일을 이용한 사용자 조회 기능을 제공한다

