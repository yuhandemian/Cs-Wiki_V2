package io.ata.prompt

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PromptApplication

fun main(args: Array<String>) {
    runApplication<PromptApplication>(*args)
}
