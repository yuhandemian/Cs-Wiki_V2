package io.ata.auth.repository

import io.ata.auth.domain.User
import org.springframework.data.jpa.repository.JpaRepository

// Spring Data JPA Repository입니다.
// JpaRepository<User, Long>만 상속해도 기본 CRUD 메서드가 생깁니다.
interface UserRepository : JpaRepository<User, Long> {
    // 메서드 이름을 보고 Spring Data가 SELECT ... WHERE email = ? 쿼리를 자동 생성합니다.
    fun findByEmail(email: String): User?

    // 중복 이메일 검사에 사용합니다. 전체 User를 가져오지 않고 존재 여부만 확인할 수 있습니다.
    fun existsByEmail(email: String): Boolean
}
