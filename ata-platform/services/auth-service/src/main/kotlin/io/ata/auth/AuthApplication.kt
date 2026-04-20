package io.ata.auth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
// auth-service의 시작점입니다.
// 이 클래스가 있는 io.ata.auth 패키지 하위의 Controller/Service/Repository가 component scan 대상이 됩니다.
class AuthApplication

fun main(args: Array<String>) {
    // Kotlin의 *args는 Java varargs에 배열을 펼쳐 넘기는 spread operator입니다.
    runApplication<AuthApplication>(*args)
}
