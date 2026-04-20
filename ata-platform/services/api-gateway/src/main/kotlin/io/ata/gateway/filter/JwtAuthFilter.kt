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
class JwtAuthFilter(
    @Value("\${jwt.secret}") private val jwtSecret: String
) : AbstractGatewayFilterFactory<JwtAuthFilter.Config>(Config::class.java) {

    private val signingKey by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    class Config

    override fun apply(config: Config): GatewayFilter = GatewayFilter { exchange, chain ->
        val token = extractToken(exchange)
            ?: return@GatewayFilter unauthorized(exchange, "Authorization header missing")

        try {
            val claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .payload

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
            unauthorized(exchange, "Invalid token")
        }
    }

    private fun extractToken(exchange: ServerWebExchange): String? {
        val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION) ?: return null
        return if (authHeader.startsWith("Bearer ")) authHeader.substring(7) else null
    }

    private fun unauthorized(exchange: ServerWebExchange, message: String): Mono<Void> {
        exchange.response.statusCode = HttpStatus.UNAUTHORIZED
        exchange.response.headers.add("Content-Type", "application/json")
        val body = """{"success":false,"message":"$message"}""".toByteArray()
        val buffer = exchange.response.bufferFactory().wrap(body)
        return exchange.response.writeWith(Mono.just(buffer))
    }
}
