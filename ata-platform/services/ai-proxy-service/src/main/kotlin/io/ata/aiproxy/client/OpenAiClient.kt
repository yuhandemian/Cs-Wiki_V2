package io.ata.aiproxy.client

import io.ata.aiproxy.config.AiProviderProperties
import io.ata.aiproxy.dto.ChatMessage
import io.ata.aiproxy.dto.ChatResponse
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.core.publisher.Flux

@Component
// OpenAI Chat Completions API를 호출하는 client입니다.
// 외부 API 형식을 내부 표준 ChatResponse/Flux<String> 형태로 변환합니다.
class OpenAiClient(
    properties: AiProviderProperties,
    webClientBuilder: WebClient.Builder
) : LlmClient {

    private val config = properties.openai
    // WebClient는 Spring의 reactive HTTP client입니다.
    // baseUrl과 공통 header를 미리 설정해두고 요청마다 body만 바꿉니다.
    private val webClient = webClientBuilder
        .baseUrl(config.baseUrl)
        .defaultHeader("Authorization", "Bearer ${config.apiKey}")
        .defaultHeader("Content-Type", "application/json")
        .build()

    override suspend fun chat(
        messages: List<ChatMessage>,
        model: String?,
        temperature: Double,
        maxTokens: Int
    ): ChatResponse {
        // 요청에서 model이 생략되면 application.yml의 default-model을 사용합니다.
        val requestModel = model ?: config.defaultModel
        // OpenAI Chat Completions API 요청 body입니다.
        val body = mapOf(
            "model" to requestModel,
            "messages" to messages.map { mapOf("role" to it.role, "content" to it.content) },
            "temperature" to temperature,
            "max_tokens" to maxTokens
        )
        val response = webClient.post()
            .uri("/chat/completions")
            .bodyValue(body)
            .retrieve()
            .awaitBody<Map<String, Any>>()

        @Suppress("UNCHECKED_CAST")
        // 응답을 Map으로 받은 뒤 필요한 필드를 꺼냅니다.
        // 운영 코드에서는 provider별 response DTO를 두면 타입 안정성이 더 좋아집니다.
        val choices = response["choices"] as List<Map<String, Any>>
        val message = (choices[0]["message"] as Map<String, Any>)["content"] as String
        val usage = response["usage"] as Map<String, Any>

        return ChatResponse(
            provider = "OPENAI",
            model = requestModel,
            content = message,
            inputTokens = (usage["prompt_tokens"] as Int),
            outputTokens = (usage["completion_tokens"] as Int)
        )
    }

    override fun chatStream(
        messages: List<ChatMessage>,
        model: String?,
        temperature: Double,
        maxTokens: Int
    ): Flux<String> {
        val requestModel = model ?: config.defaultModel
        // stream=true를 넣으면 OpenAI가 SSE 형식으로 token chunk를 내려줍니다.
        val body = mapOf(
            "model" to requestModel,
            "messages" to messages.map { mapOf("role" to it.role, "content" to it.content) },
            "temperature" to temperature,
            "max_tokens" to maxTokens,
            "stream" to true
        )
        return webClient.post()
            .uri("/chat/completions")
            .bodyValue(body)
            .retrieve()
            .bodyToFlux(String::class.java)
            // OpenAI stream은 "data: ..." 라인으로 내려오며 마지막은 data: [DONE]입니다.
            .filter { it.startsWith("data: ") && it != "data: [DONE]" }
            .map { it.removePrefix("data: ") }
    }
}
