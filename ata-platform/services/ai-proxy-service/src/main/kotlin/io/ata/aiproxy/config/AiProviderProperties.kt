package io.ata.aiproxy.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AiProviderProperties::class)
class AiPropertiesConfig

@ConfigurationProperties(prefix = "ai.providers")
data class AiProviderProperties(
    val openai: ProviderConfig = ProviderConfig(),
    val anthropic: AnthropicConfig = AnthropicConfig(),
    val gemini: ProviderConfig = ProviderConfig(),
    val grok: ProviderConfig = ProviderConfig(),
    val perplexity: ProviderConfig = ProviderConfig(),
    val deepseek: ProviderConfig = ProviderConfig()
)

data class ProviderConfig(
    val baseUrl: String = "",
    val apiKey: String = "",
    val defaultModel: String = ""
)

data class AnthropicConfig(
    val baseUrl: String = "",
    val apiKey: String = "",
    val defaultModel: String = "",
    val version: String = "2023-06-01"
)
