package io.ata.auth.service

import io.ata.auth.domain.AuthProvider
import io.ata.auth.domain.User
import io.ata.auth.dto.*
import io.ata.auth.exception.DuplicateEmailException
import io.ata.auth.exception.InvalidCredentialsException
import io.ata.auth.repository.UserRepository
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Service
@Transactional(readOnly = true)
class AuthService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder,
    private val redisTemplate: StringRedisTemplate
) {
    @Transactional
    fun signUp(request: SignUpRequest): TokenResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw DuplicateEmailException(request.email)
        }
        val user = userRepository.save(
            User(
                email = request.email,
                password = passwordEncoder.encode(request.password),
                name = request.name,
                provider = AuthProvider.LOCAL
            )
        )
        return issueTokens(user)
    }

    fun signIn(request: SignInRequest): TokenResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw InvalidCredentialsException()
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw InvalidCredentialsException()
        }
        return issueTokens(user)
    }

    fun refresh(request: RefreshRequest): TokenResponse {
        val userId = runCatching { jwtService.validateAndGetUserId(request.refreshToken) }
            .getOrElse { throw InvalidCredentialsException() }

        val blacklistKey = "blacklist:refresh:${request.refreshToken}"
        if (redisTemplate.hasKey(blacklistKey) == true) {
            throw InvalidCredentialsException()
        }

        val user = userRepository.findById(userId).orElseThrow { InvalidCredentialsException() }

        // 기존 refresh token 무효화 (rotation)
        redisTemplate.opsForValue().set(blacklistKey, "1", 30, TimeUnit.DAYS)

        return issueTokens(user)
    }

    fun signOut(refreshToken: String) {
        val blacklistKey = "blacklist:refresh:$refreshToken"
        redisTemplate.opsForValue().set(blacklistKey, "1", 30, TimeUnit.DAYS)
    }

    private fun issueTokens(user: User): TokenResponse {
        val accessToken = jwtService.generateAccessToken(user.id, user.email, user.plan.name)
        val refreshToken = jwtService.generateRefreshToken(user.id)
        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtService.getAccessTokenExpiry() / 1000
        )
    }
}
