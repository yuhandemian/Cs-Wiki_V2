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
// AI 호출 API의 HTTP 입구입니다.
// Controller는 일반 응답, streaming 응답, multi-provider 응답을 각각 다른 endpoint로 제공합니다.
class AiProxyController(private val aiProxyService: AiProxyService) {

    // 단일 AI 채팅 (일반 응답)
    @PostMapping("/chat")
    // suspend 함수는 Kotlin coroutine 기반 비동기 함수입니다.
    // 외부 LLM API 응답을 기다리는 동안 thread를 효율적으로 사용할 수 있습니다.
    suspend fun chat(@Valid @RequestBody request: SingleChatRequest): ApiResponse<ChatResponse> =
        ApiResponse.success(aiProxyService.chat(request))

    // 단일 AI 채팅 (SSE 스트리밍) — ATA 핵심 기능
    @PostMapping("/chat/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    // SSE(Server-Sent Events)는 서버가 여러 조각의 응답을 순차적으로 밀어주는 방식입니다.
    // Reactor Flux는 0개 이상의 비동기 이벤트 흐름을 표현합니다.
    fun chatStream(@Valid @RequestBody request: SingleChatRequest): Flux<ServerSentEvent<ChatChunk>> =
        aiProxyService.chatStream(request)
            .map { chunk ->
                // chunk.done 여부에 따라 프론트엔드가 일반 조각과 완료 이벤트를 구분할 수 있게 합니다.
                ServerSentEvent.builder(chunk)
                    .event(if (chunk.done) "done" else "chunk")
                    .build()
            }

    // 멀티 AI 동시 비교 — ATA 핵심 기능 01
    @PostMapping("/chat/multi")
    // 같은 메시지를 여러 provider에 동시에 보내고 provider별 성공/실패 결과를 묶어서 반환합니다.
    suspend fun multiChat(@Valid @RequestBody request: MultiChatRequest): ApiResponse<MultiChatResponse> =
        ApiResponse.success(aiProxyService.multiChat(request))
}

// ai-proxy-service 전용 공통 응답 wrapper입니다.
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
