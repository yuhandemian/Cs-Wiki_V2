package io.ata.gateway.filter

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
// Spring Cloud Gateway에서 쓰는 커스텀 필터입니다.
// 보호된 route에 붙어서 JWT를 검증하고, 내부 서비스가 사용할 사용자 헤더를 추가합니다.
class JwtAuthFilter(
    @Value("\${jwt.secret}") private val jwtSecret: String
) : AbstractGatewayFilterFactory<JwtAuthFilter.Config>(Config::class.java) {

    // auth-service의 JwtService와 같은 secret을 사용해야 같은 token을 검증할 수 있습니다.
    private val signingKey by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    // GatewayFilterFactory는 설정 객체 타입이 필요합니다.
    // 현재는 별도 설정값이 없어서 빈 Config 클래스를 둡니다.
    class Config

    override fun apply(config: Config): GatewayFilter = GatewayFilter { exchange, chain ->
        // Authorization: Bearer <token> 형태가 아니면 바로 401을 반환합니다.
        val token = extractToken(exchange)
            ?: return@GatewayFilter unauthorized(exchange, "Authorization header missing")

        try {
            // 서명, 만료 시간, JWT 형식이 모두 유효해야 payload를 얻을 수 있습니다.
            val claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .payload

            // Gateway는 downstream service 앞단에 있으므로,
            // 검증된 JWT claim을 내부 헤더로 바꿔 전달합니다.
            // prompt-service/chat-service는 이 헤더를 보고 현재 사용자를 판단합니다.
            val mutatedExchange = exchange.mutate()
                .request(
                    exchange.request.mutate()
                        .header("X-User-Id", claims.subject)
                        .header("X-User-Email", claims["email"]?.toString() ?: "")
                        .header("X-User-Plan", claims["plan"]?.toString() ?: "FREE")
                        .build()
                ).build()

            chain.filter(mutatedExchange)
        } catch (e: JwtException) {
            // 만료, 서명 불일치, malformed token 등 jjwt 계열 검증 실패를 모두 401로 처리합니다.
            unauthorized(exchange, "Invalid token")
        }
    }

    private fun extractToken(exchange: ServerWebExchange): String? {
        val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION) ?: return null
        // "Bearer "는 7글자입니다. 그 뒤의 실제 JWT 문자열만 잘라냅니다.
        return if (authHeader.startsWith("Bearer ")) authHeader.substring(7) else null
    }

    private fun unauthorized(exchange: ServerWebExchange, message: String): Mono<Void> {
        // Gateway는 WebFlux 기반이라 응답 body도 Mono<Void>로 비동기 작성합니다.
        exchange.response.statusCode = HttpStatus.UNAUTHORIZED
        exchange.response.headers.add("Content-Type", "application/json")
        val body = """{"success":false,"message":"$message"}""".toByteArray()
        val buffer = exchange.response.bufferFactory().wrap(body)
        return exchange.response.writeWith(Mono.just(buffer))
    }
}
