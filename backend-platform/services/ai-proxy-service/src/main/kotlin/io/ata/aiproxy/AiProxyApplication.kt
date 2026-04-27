package io.ata.aiproxy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
// ai-proxy-service의 시작점입니다.
// 여러 LLM provider API를 하나의 내부 API 형태로 감싸는 서비스입니다.
class AiProxyApplication

fun main(args: Array<String>) {
    // application.yml 기준 기본 포트는 8083입니다.
    runApplication<AiProxyApplication>(*args)
}
