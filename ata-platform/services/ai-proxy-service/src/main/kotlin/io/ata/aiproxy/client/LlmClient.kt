package io.ata.aiproxy.client

import io.ata.aiproxy.dto.ChatMessage
import io.ata.aiproxy.dto.ChatResponse
import reactor.core.publisher.Flux

// provider별 client가 맞춰야 하는 공통 인터페이스입니다.
// AiProxyService는 이 인터페이스만 보고 호출하므로, provider별 API 차이는 client 내부에 숨겨집니다.
interface LlmClient {
    // 일반 응답: 전체 답변이 완성된 뒤 ChatResponse 하나로 반환합니다.
    suspend fun chat(messages: List<ChatMessage>, model: String?, temperature: Double, maxTokens: Int): ChatResponse

    // streaming 응답: 답변 조각을 Flux<String>으로 순차 반환합니다.
    fun chatStream(messages: List<ChatMessage>, model: String?, temperature: Double, maxTokens: Int): Flux<String>
}
