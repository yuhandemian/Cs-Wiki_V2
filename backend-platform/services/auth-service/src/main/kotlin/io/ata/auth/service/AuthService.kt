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
// 클래스 전체의 기본 트랜잭션을 readOnly로 둡니다.
// DB를 변경하는 signUp에는 아래에서 별도로 @Transactional을 붙여 write transaction으로 바꿉니다.
@Transactional(readOnly = true)
// Kotlin 생성자 주입입니다.
// Java라면 final field와 constructor를 따로 쓰지만, Kotlin은 클래스 선언부에서 바로 표현할 수 있습니다.
class AuthService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder,
    private val redisTemplate: StringRedisTemplate
) {
    @Transactional
    fun signUp(request: SignUpRequest): TokenResponse {
        // 이메일은 users.email에 unique 제약이 있지만,
        // 사용자에게 명확한 에러를 주기 위해 저장 전에 한 번 더 검사합니다.
        if (userRepository.existsByEmail(request.email)) {
            throw DuplicateEmailException(request.email)
        }

        // 비밀번호는 원문으로 저장하지 않고 BCrypt hash로 저장합니다.
        // provider를 LOCAL로 두는 것은 OAuth가 아닌 이메일/비밀번호 가입이라는 뜻입니다.
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
        // ?: 는 Elvis operator입니다.
        // 왼쪽 값이 null이면 오른쪽 예외를 실행합니다.
        val user = userRepository.findByEmail(request.email)
            ?: throw InvalidCredentialsException()

        // 사용자 없음과 비밀번호 불일치를 같은 예외로 처리합니다.
        // 외부에 "가입된 이메일인지"를 알려주지 않기 위한 흔한 인증 보안 패턴입니다.
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw InvalidCredentialsException()
        }
        return issueTokens(user)
    }

    fun refresh(request: RefreshRequest): TokenResponse {
        // refresh token 자체가 유효한 JWT인지 먼저 확인하고,
        // subject에 들어있는 userId를 꺼냅니다. 검증 실패는 모두 인증 실패로 통일합니다.
        val userId = runCatching { jwtService.validateAndGetUserId(request.refreshToken) }
            .getOrElse { throw InvalidCredentialsException() }

        // refresh token blacklist는 Redis에 저장합니다.
        // 로그아웃했거나 이미 한 번 사용된 refresh token이면 재발급을 막습니다.
        val blacklistKey = "blacklist:refresh:${request.refreshToken}"
        if (redisTemplate.hasKey(blacklistKey) == true) {
            throw InvalidCredentialsException()
        }

        // token은 유효하지만 DB에서 사용자가 사라진 경우도 막아야 합니다.
        val user = userRepository.findById(userId).orElseThrow { InvalidCredentialsException() }

        // 기존 refresh token 무효화 (rotation)
        // refresh token rotation: 한 번 재발급에 사용한 refresh token을 바로 폐기합니다.
        redisTemplate.opsForValue().set(blacklistKey, "1", 30, TimeUnit.DAYS)

        return issueTokens(user)
    }

    fun signOut(refreshToken: String) {
        // 로그아웃은 refresh token을 blacklist에 넣는 것으로 처리합니다.
        // 만료 시간은 refresh token 만료 기간과 맞추는 것이 이상적이며, 여기서는 30일로 고정되어 있습니다.
        val blacklistKey = "blacklist:refresh:$refreshToken"
        redisTemplate.opsForValue().set(blacklistKey, "1", 30, TimeUnit.DAYS)
    }

    private fun issueTokens(user: User): TokenResponse {
        // access token에는 gateway와 downstream service가 바로 쓸 사용자 정보를 claim으로 넣습니다.
        // refresh token은 재발급 용도라 subject(userId)만 담아 더 단순하게 유지합니다.
        val accessToken = jwtService.generateAccessToken(user.id, user.email, user.plan.name)
        val refreshToken = jwtService.generateRefreshToken(user.id)
        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtService.getAccessTokenExpiry() / 1000
        )
    }
}
