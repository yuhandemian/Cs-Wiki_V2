package io.ata.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
// api-gateway의 시작점입니다.
// Gateway route 설정은 application.yml에 있고, 커스텀 JWT 필터는 component scan으로 등록됩니다.
class GatewayApplication

fun main(args: Array<String>) {
    // Spring Boot 내장 서버를 실행합니다. gateway는 기본적으로 8080 포트에서 뜹니다.
    runApplication<GatewayApplication>(*args)
}
