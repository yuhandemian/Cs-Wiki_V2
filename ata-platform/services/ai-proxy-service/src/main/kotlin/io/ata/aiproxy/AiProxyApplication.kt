package io.ata.aiproxy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AiProxyApplication

fun main(args: Array<String>) {
    runApplication<AiProxyApplication>(*args)
}
