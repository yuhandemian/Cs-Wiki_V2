package io.ata.auth.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class JwtService(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.access-token-expiry}") private val accessTokenExpiry: Long,
    @Value("\${jwt.refresh-token-expiry}") private val refreshTokenExpiry: Long
) {
    private val signingKey by lazy { Keys.hmacShaKeyFor(secret.toByteArray()) }

    fun generateAccessToken(userId: Long, email: String, plan: String): String =
        Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("plan", plan)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + accessTokenExpiry))
            .signWith(signingKey)
            .compact()

    fun generateRefreshToken(userId: Long): String =
        Jwts.builder()
            .subject(userId.toString())
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + refreshTokenExpiry))
            .signWith(signingKey)
            .compact()

    fun validateAndGetUserId(token: String): Long =
        Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject
            .toLong()

    fun getAccessTokenExpiry(): Long = accessTokenExpiry
}
