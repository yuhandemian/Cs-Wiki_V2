package io.ata.aiproxy.service

import io.ata.aiproxy.client.AnthropicClient
import io.ata.aiproxy.client.LlmClient
import io.ata.aiproxy.client.OpenAiClient
import io.ata.aiproxy.domain.LlmProvider
import io.ata.aiproxy.dto.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class AiProxyService(
    private val openAiClient: OpenAiClient,
    private val anthropicClient: AnthropicClient
) {
    private fun getClient(provider: LlmProvider): LlmClient = when (provider) {
        LlmProvider.OPENAI -> openAiClient
        LlmProvider.ANTHROPIC -> anthropicClient
        else -> throw UnsupportedOperationException("${provider.name} 연동 준비 중")
    }

    suspend fun chat(request: SingleChatRequest): ChatResponse {
        val provider = LlmProvider.valueOf(request.provider.uppercase())
        return getClient(provider).chat(
            request.messages, request.model, request.temperature, request.maxTokens
        )
    }

    fun chatStream(request: SingleChatRequest): Flux<ChatChunk> {
        val provider = LlmProvider.valueOf(request.provider.uppercase())
        return getClient(provider)
            .chatStream(request.messages, request.model, request.temperature, request.maxTokens)
            .map { ChatChunk(provider = request.provider, content = it) }
            .concatWith(Flux.just(ChatChunk(provider = request.provider, content = "", done = true)))
            .onErrorResume { e ->
                Flux.just(ChatChunk(provider = request.provider, content = "", error = e.message))
            }
    }

    // ATA 핵심: 여러 AI에 동시에 같은 질문을 보내고 결과 비교
    suspend fun multiChat(request: MultiChatRequest): MultiChatResponse = coroutineScope {
        val jobs = request.providers.map { providerName ->
            providerName to async {
                runCatching {
                    val provider = LlmProvider.valueOf(providerName.uppercase())
                    getClient(provider).chat(
                        request.messages, null, request.temperature, request.maxTokens
                    )
                }
            }
        }

        val results = jobs.associate { (providerName, deferred) ->
            val result = deferred.await()
            providerName to if (result.isSuccess) {
                ChatResponseOrError(success = true, data = result.getOrNull())
            } else {
                ChatResponseOrError(success = false, error = result.exceptionOrNull()?.message)
            }
        }

        MultiChatResponse(results)
    }
}
