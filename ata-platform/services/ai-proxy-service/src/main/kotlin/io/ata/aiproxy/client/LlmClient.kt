package io.ata.aiproxy.client

import io.ata.aiproxy.dto.ChatMessage
import io.ata.aiproxy.dto.ChatResponse
import reactor.core.publisher.Flux

interface LlmClient {
    suspend fun chat(messages: List<ChatMessage>, model: String?, temperature: Double, maxTokens: Int): ChatResponse
    fun chatStream(messages: List<ChatMessage>, model: String?, temperature: Double, maxTokens: Int): Flux<String>
}
