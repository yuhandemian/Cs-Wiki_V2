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
// provider 선택과 orchestration을 담당하는 서비스입니다.
// 실제 HTTP 호출 세부사항은 OpenAiClient, AnthropicClient 같은 client 클래스로 분리되어 있습니다.
class AiProxyService(
    private val openAiClient: OpenAiClient,
    private val anthropicClient: AnthropicClient
) {
    // 내부 표준 provider enum을 실제 client 구현체로 매핑합니다.
    // 현재는 OpenAI/Anthropic만 구현되어 있고, 나머지는 확장 예정입니다.
    private fun getClient(provider: LlmProvider): LlmClient = when (provider) {
        LlmProvider.OPENAI -> openAiClient
        LlmProvider.ANTHROPIC -> anthropicClient
        else -> throw UnsupportedOperationException("${provider.name} 연동 준비 중")
    }

    suspend fun chat(request: SingleChatRequest): ChatResponse {
        // 프론트엔드가 "openai"처럼 보내도 enum 이름에 맞게 대문자로 변환합니다.
        // 잘못된 provider 이름이면 IllegalArgumentException이 발생합니다.
        val provider = LlmProvider.valueOf(request.provider.uppercase())
        return getClient(provider).chat(
            request.messages, request.model, request.temperature, request.maxTokens
        )
    }

    fun chatStream(request: SingleChatRequest): Flux<ChatChunk> {
        val provider = LlmProvider.valueOf(request.provider.uppercase())
        return getClient(provider)
            // client는 provider별 raw text stream을 Flux<String>으로 돌려줍니다.
            .chatStream(request.messages, request.model, request.temperature, request.maxTokens)
            // 외부 API 조각을 내부 표준 ChatChunk 형태로 감쌉니다.
            .map { ChatChunk(provider = request.provider, content = it) }
            // stream이 정상 종료되면 done=true chunk를 하나 추가해 프론트가 완료를 알 수 있게 합니다.
            .concatWith(Flux.just(ChatChunk(provider = request.provider, content = "", done = true)))
            .onErrorResume { e ->
                // streaming 중 에러가 나도 Flux 자체를 깨뜨리기보다 error chunk를 내려줍니다.
                Flux.just(ChatChunk(provider = request.provider, content = "", error = e.message))
            }
    }

    // ATA 핵심: 여러 AI에 동시에 같은 질문을 보내고 결과 비교
    suspend fun multiChat(request: MultiChatRequest): MultiChatResponse = coroutineScope {
        // coroutineScope 안에서 async를 쓰면 provider별 호출을 병렬로 시작할 수 있습니다.
        val jobs = request.providers.map { providerName ->
            providerName to async {
                // provider 하나가 실패해도 전체 multiChat이 실패하지 않도록 runCatching으로 감쌉니다.
                runCatching {
                    val provider = LlmProvider.valueOf(providerName.uppercase())
                    getClient(provider).chat(
                        request.messages, null, request.temperature, request.maxTokens
                    )
                }
            }
        }

        // await로 각 provider 결과를 기다린 뒤, 성공/실패를 provider 이름별 map으로 정리합니다.
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
