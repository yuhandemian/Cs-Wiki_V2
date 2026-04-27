package io.ata.auth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
// auth-service 자체의 Spring Security 설정입니다.
// gateway에서 JWT를 검사하더라도 auth-service는 독립 실행될 수 있으므로 별도 보안 설정을 가집니다.
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain =
        http
            // REST API 서버에서는 브라우저 form 기반 CSRF 토큰을 쓰지 않으므로 비활성화합니다.
            .csrf { it.disable() }
            // JWT 기반 인증은 서버 세션을 만들지 않는 stateless 방식입니다.
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                // 회원가입/로그인/refresh/logout과 health check는 인증 없이 접근 가능해야 합니다.
                auth.requestMatchers("/auth/**", "/actuator/health").permitAll()
                    .anyRequest().authenticated()
            }
            .build()

    // BCrypt는 같은 비밀번호도 매번 다른 hash가 나오도록 salt를 사용합니다.
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
