package io.ata.aiproxy.dto

import io.ata.aiproxy.domain.LlmProvider
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

// 내부 표준 chat message 형식입니다.
// provider마다 외부 API 형식은 다르지만, Controller와 Service는 이 형식만 다룹니다.
data class ChatMessage(
    val role: String,  // "user" | "assistant" | "system"
    val content: String
)

// 단일 provider에 chat 요청을 보낼 때 사용하는 DTO입니다.
data class SingleChatRequest(
    @field:NotBlank val provider: String,
    // null이면 provider별 default model을 사용합니다.
    val model: String? = null,
    @field:NotEmpty val messages: List<ChatMessage>,
    val temperature: Double = 0.7,
    val maxTokens: Int = 2048,
    val stream: Boolean = false
)

// 여러 provider에 같은 메시지를 동시에 보낼 때 사용하는 DTO입니다.
data class MultiChatRequest(
    @field:NotEmpty val providers: List<String>,
    @field:NotEmpty val messages: List<ChatMessage>,
    val temperature: Double = 0.7,
    val maxTokens: Int = 2048
)

// streaming 응답의 표준 chunk입니다.
// content에는 provider가 내려준 조각이 들어가고, done/error로 상태를 표현합니다.
data class ChatChunk(
    val provider: String,
    val content: String,
    val done: Boolean = false,
    val error: String? = null
)

// 일반 chat 응답의 표준 형태입니다.
// provider별 token usage 필드 이름이 달라도 여기서는 input/output token으로 맞춥니다.
data class ChatResponse(
    val provider: String,
    val model: String,
    val content: String,
    val inputTokens: Int,
    val outputTokens: Int
)

// multi chat은 provider 이름을 key로 하는 결과 map을 반환합니다.
data class MultiChatResponse(
    val results: Map<String, ChatResponseOrError>
)

// multi chat에서 provider 하나가 실패해도 전체 응답은 성공할 수 있게 성공/실패를 개별 표현합니다.
data class ChatResponseOrError(
    val success: Boolean,
    val data: ChatResponse? = null,
    val error: String? = null
)
