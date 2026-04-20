package io.ata.aiproxy.client

import io.ata.aiproxy.config.AiProviderProperties
import io.ata.aiproxy.dto.ChatMessage
import io.ata.aiproxy.dto.ChatResponse
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.core.publisher.Flux

@Component
class AnthropicClient(
    properties: AiProviderProperties,
    webClientBuilder: WebClient.Builder
) : LlmClient {

    private val config = properties.anthropic
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
        val (systemMsg, userMessages) = splitSystemMessage(messages)

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
            .filter { it.startsWith("data:") }
            .map { it.removePrefix("data:").trim() }
    }

    private fun splitSystemMessage(messages: List<ChatMessage>): Pair<String?, List<ChatMessage>> {
        val system = messages.firstOrNull { it.role == "system" }?.content
        val rest = messages.filter { it.role != "system" }
        return Pair(system, rest)
    }
}
