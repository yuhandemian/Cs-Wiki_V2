package io.ata.auth.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
// JWT 생성/검증만 담당하는 작은 서비스입니다.
// 인증 정책은 AuthService에 있고, 이 클래스는 token 포맷과 서명 처리를 캡슐화합니다.
class JwtService(
    // application.yml의 jwt.* 값을 주입받습니다.
    // 운영에서는 JWT_SECRET 환경 변수로 반드시 강한 secret을 넣어야 합니다.
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.access-token-expiry}") private val accessTokenExpiry: Long,
    @Value("\${jwt.refresh-token-expiry}") private val refreshTokenExpiry: Long
) {
    // by lazy는 처음 사용할 때 한 번만 signing key를 생성하고 이후 재사용합니다.
    private val signingKey by lazy { Keys.hmacShaKeyFor(secret.toByteArray()) }

    fun generateAccessToken(userId: Long, email: String, plan: String): String =
        Jwts.builder()
            // JWT subject는 token의 주 식별자입니다. 여기서는 userId를 문자열로 저장합니다.
            .subject(userId.toString())
            // gateway가 downstream service에 전달할 사용자 정보를 claim으로 넣습니다.
            .claim("email", email)
            .claim("plan", plan)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + accessTokenExpiry))
            .signWith(signingKey)
            .compact()

    fun generateRefreshToken(userId: Long): String =
        Jwts.builder()
            // refresh token은 access token 재발급만 담당하므로 최소 정보인 userId만 담습니다.
            .subject(userId.toString())
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + refreshTokenExpiry))
            .signWith(signingKey)
            .compact()

    fun validateAndGetUserId(token: String): Long =
        Jwts.parser()
            // 서명 검증에 실패하거나 만료된 token이면 jjwt가 예외를 던집니다.
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject
            .toLong()

    // 프론트엔드가 "몇 초 뒤 access token이 만료되는지" 알 수 있게 응답에 포함합니다.
    fun getAccessTokenExpiry(): Long = accessTokenExpiry
}
