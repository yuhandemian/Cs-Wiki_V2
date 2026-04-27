package io.ata.aiproxy.domain

// 서비스 내부에서 지원 provider를 표준화하기 위한 enum입니다.
// enum에 있다고 해서 client 구현이 모두 끝난 것은 아니며, 현재 AiProxyService는 OpenAI/Anthropic만 매핑합니다.
enum class LlmProvider(val displayName: String) {
    OPENAI("GPT"),
    ANTHROPIC("Claude"),
    GEMINI("Gemini"),
    GROK("Grok"),
    PERPLEXITY("Perplexity"),
    DEEPSEEK("DeepSeek")
}
