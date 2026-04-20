package io.ata.aiproxy.controller

import io.ata.aiproxy.dto.*
import io.ata.aiproxy.service.AiProxyService
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/ai")
class AiProxyController(private val aiProxyService: AiProxyService) {

    // 단일 AI 채팅 (일반 응답)
    @PostMapping("/chat")
    suspend fun chat(@Valid @RequestBody request: SingleChatRequest): ApiResponse<ChatResponse> =
        ApiResponse.success(aiProxyService.chat(request))

    // 단일 AI 채팅 (SSE 스트리밍) — ATA 핵심 기능
    @PostMapping("/chat/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun chatStream(@Valid @RequestBody request: SingleChatRequest): Flux<ServerSentEvent<ChatChunk>> =
        aiProxyService.chatStream(request)
            .map { chunk ->
                ServerSentEvent.builder(chunk)
                    .event(if (chunk.done) "done" else "chunk")
                    .build()
            }

    // 멀티 AI 동시 비교 — ATA 핵심 기능 01
    @PostMapping("/chat/multi")
    suspend fun multiChat(@Valid @RequestBody request: MultiChatRequest): ApiResponse<MultiChatResponse> =
        ApiResponse.success(aiProxyService.multiChat(request))
}

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
) {
    companion object {
        fun <T> success(data: T) = ApiResponse(true, data)
        fun error(message: String) = ApiResponse<Nothing>(false, message = message)
    }
}
