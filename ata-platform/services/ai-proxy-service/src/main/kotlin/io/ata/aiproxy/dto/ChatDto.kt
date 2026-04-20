package io.ata.aiproxy.dto

import io.ata.aiproxy.domain.LlmProvider
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class ChatMessage(
    val role: String,  // "user" | "assistant" | "system"
    val content: String
)

data class SingleChatRequest(
    @field:NotBlank val provider: String,
    val model: String? = null,
    @field:NotEmpty val messages: List<ChatMessage>,
    val temperature: Double = 0.7,
    val maxTokens: Int = 2048,
    val stream: Boolean = false
)

data class MultiChatRequest(
    @field:NotEmpty val providers: List<String>,
    @field:NotEmpty val messages: List<ChatMessage>,
    val temperature: Double = 0.7,
    val maxTokens: Int = 2048
)

data class ChatChunk(
    val provider: String,
    val content: String,
    val done: Boolean = false,
    val error: String? = null
)

data class ChatResponse(
    val provider: String,
    val model: String,
    val content: String,
    val inputTokens: Int,
    val outputTokens: Int
)

data class MultiChatResponse(
    val results: Map<String, ChatResponseOrError>
)

data class ChatResponseOrError(
    val success: Boolean,
    val data: ChatResponse? = null,
    val error: String? = null
)
