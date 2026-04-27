package io.ata.aiproxy.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AiProviderProperties::class)
// @ConfigurationProperties 클래스를 Spring Bean으로 등록합니다.
// application.yml의 ai.providers.* 값이 AiProviderProperties에 바인딩됩니다.
class AiPropertiesConfig

@ConfigurationProperties(prefix = "ai.providers")
// provider별 baseUrl/apiKey/defaultModel 설정을 한 객체로 묶습니다.
// 새 provider를 추가할 때 여기에 설정을 추가하고 client를 구현하면 됩니다.
data class AiProviderProperties(
    val openai: ProviderConfig = ProviderConfig(),
    val anthropic: AnthropicConfig = AnthropicConfig(),
    val gemini: ProviderConfig = ProviderConfig(),
    val grok: ProviderConfig = ProviderConfig(),
    val perplexity: ProviderConfig = ProviderConfig(),
    val deepseek: ProviderConfig = ProviderConfig()
)

// 대부분 provider가 공유하는 기본 설정 구조입니다.
data class ProviderConfig(
    val baseUrl: String = "",
    val apiKey: String = "",
    val defaultModel: String = ""
)

// Anthropic은 anthropic-version header가 추가로 필요하므로 별도 config를 둡니다.
data class AnthropicConfig(
    val baseUrl: String = "",
    val apiKey: String = "",
    val defaultModel: String = "",
    val version: String = "2023-06-01"
)
