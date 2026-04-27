package io.ata.prompt

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
// prompt-service의 시작점입니다.
// /api/prompts 하위 API와 JPA Repository들이 이 애플리케이션 컨텍스트에 등록됩니다.
class PromptApplication

fun main(args: Array<String>) {
    // Spring Boot 애플리케이션을 실행합니다. application.yml 기준 기본 포트는 8084입니다.
    runApplication<PromptApplication>(*args)
}
