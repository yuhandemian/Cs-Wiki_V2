package io.ata.chat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
// chat-service의 시작점입니다.
// MongoDB 기반 대화 저장/조회 API가 이 애플리케이션에서 실행됩니다.
class ChatApplication

fun main(args: Array<String>) {
    // application.yml 기준 기본 포트는 8082입니다.
    runApplication<ChatApplication>(*args)
}
