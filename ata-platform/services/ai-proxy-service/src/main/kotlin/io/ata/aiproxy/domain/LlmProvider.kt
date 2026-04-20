package io.ata.aiproxy.domain

enum class LlmProvider(val displayName: String) {
    OPENAI("GPT"),
    ANTHROPIC("Claude"),
    GEMINI("Gemini"),
    GROK("Grok"),
    PERPLEXITY("Perplexity"),
    DEEPSEEK("DeepSeek")
}
