package io.ata.aiproxy.client

import io.ata.aiproxy.config.AiProviderProperties
import io.ata.aiproxy.dto.ChatMessage
import io.ata.aiproxy.dto.ChatResponse
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.core.publisher.Flux

@Component
// Anthropic Messages API를 호출하는 client입니다.
// OpenAI와 요청/응답 형식이 다르기 때문에 별도 client로 분리되어 있습니다.
class AnthropicClient(
    properties: AiProviderProperties,
    webClientBuilder: WebClient.Builder
) : LlmClient {

    private val config = properties.anthropic
    // Anthropic은 Authorization Bearer 대신 x-api-key와 anthropic-version header를 사용합니다.
    private val webClient = webClientBuilder
        .baseUrl(config.baseUrl)
        .defaultHeader("x-api-key", config.apiKey)
        .defaultHeader("anthropic-version", config.version)
        .defaultHeader("Content-Type", "application/json")
        .build()

    override suspend fun chat(
        messages: List<ChatMessage>,
        model: String?,
        temperature: Double,
        maxTokens: Int
    ): ChatResponse {
        val requestModel = model ?: config.defaultModel
        // Anthropic Messages API는 system message를 messages 배열이 아니라 별도 system 필드로 받습니다.
        val (systemMsg, userMessages) = splitSystemMessage(messages)

        // buildMap은 조건부 필드를 넣을 때 편합니다.
        // systemMsg가 null이 아닐 때만 system 필드를 추가합니다.
        val body = buildMap {
            put("model", requestModel)
            put("max_tokens", maxTokens)
            put("temperature", temperature)
            put("messages", userMessages.map { mapOf("role" to it.role, "content" to it.content) })
            systemMsg?.let { put("system", it) }
        }

        val response = webClient.post()
            .uri("/messages")
            .bodyValue(body)
            .retrieve()
            .awaitBody<Map<String, Any>>()

        @Suppress("UNCHECKED_CAST")
        // Anthropic 응답은 content 배열 안에 text block이 들어옵니다.
        val content = (response["content"] as List<Map<String, Any>>)[0]["text"] as String
        val usage = response["usage"] as Map<String, Any>

        return ChatResponse(
            provider = "ANTHROPIC",
            model = requestModel,
            content = content,
            inputTokens = (usage["input_tokens"] as Int),
            outputTokens = (usage["output_tokens"] as Int)
        )
    }

    override fun chatStream(
        messages: List<ChatMessage>,
        model: String?,
        temperature: Double,
        maxTokens: Int
    ): Flux<String> {
        val requestModel = model ?: config.defaultModel
        val (systemMsg, userMessages) = splitSystemMessage(messages)

        // stream=true를 넣어 Anthropic streaming 응답을 요청합니다.
        val body = buildMap {
            put("model", requestModel)
            put("max_tokens", maxTokens)
            put("temperature", temperature)
            put("stream", true)
            put("messages", userMessages.map { mapOf("role" to it.role, "content" to it.content) })
            systemMsg?.let { put("system", it) }
        }

        return webClient.post()
            .uri("/messages")
            .bodyValue(body)
            .retrieve()
            .bodyToFlux(String::class.java)
            // Anthropic stream도 data: 라인을 사용합니다. 여기서는 raw JSON chunk 문자열을 그대로 넘깁니다.
            .filter { it.startsWith("data:") }
            .map { it.removePrefix("data:").trim() }
    }

    // 내부 표준 ChatMessage 목록에서 system 메시지를 분리합니다.
    // OpenAI는 system role을 messages 배열에 넣지만, Anthropic은 별도 system 필드를 사용합니다.
    private fun splitSystemMessage(messages: List<ChatMessage>): Pair<String?, List<ChatMessage>> {
        val system = messages.firstOrNull { it.role == "system" }?.content
        val rest = messages.filter { it.role != "system" }
        return Pair(system, rest)
    }
}
