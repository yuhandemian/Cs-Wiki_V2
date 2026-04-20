package io.ata.aiproxy.client

import io.ata.aiproxy.config.AiProviderProperties
import io.ata.aiproxy.dto.ChatMessage
import io.ata.aiproxy.dto.ChatResponse
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.core.publisher.Flux

@Component
class OpenAiClient(
    properties: AiProviderProperties,
    webClientBuilder: WebClient.Builder
) : LlmClient {

    private val config = properties.openai
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
        val requestModel = model ?: config.defaultModel
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
            .filter { it.startsWith("data: ") && it != "data: [DONE]" }
            .map { it.removePrefix("data: ") }
    }
}
