export type LlmProvider = "OPENAI" | "ANTHROPIC" | "GEMINI" | "GROK" | "PERPLEXITY" | "DEEPSEEK"

export interface ProviderInfo {
  id: LlmProvider
  name: string
  model: string
  color: string
  bgColor: string
}

export const PROVIDERS: ProviderInfo[] = [
  { id: "OPENAI",     name: "GPT",        model: "gpt-4o",              color: "#10a37f", bgColor: "bg-[#10a37f]" },
  { id: "ANTHROPIC",  name: "Claude",     model: "claude-sonnet-4-6",   color: "#cc785c", bgColor: "bg-[#cc785c]" },
  { id: "GEMINI",     name: "Gemini",     model: "gemini-2.0-flash",    color: "#4285f4", bgColor: "bg-[#4285f4]" },
  { id: "GROK",       name: "Grok",       model: "grok-3",              color: "#ffffff", bgColor: "bg-white" },
  { id: "PERPLEXITY", name: "Perplexity", model: "sonar-pro",           color: "#20b2aa", bgColor: "bg-teal-500" },
  { id: "DEEPSEEK",   name: "DeepSeek",   model: "deepseek-chat",       color: "#0066ff", bgColor: "bg-blue-600" },
]

export interface ChatMessage {
  role: "user" | "assistant" | "system"
  content: string
}

export interface ChatResponse {
  provider: string
  model: string
  content: string
  inputTokens: number
  outputTokens: number
}

export interface MultiChatResult {
  success: boolean
  data?: ChatResponse
  error?: string
}

export interface Conversation {
  id: string
  title: string
  provider: string
  model: string
  messageCount: number
  updatedAt: string
}

export interface ConversationDetail extends Conversation {
  messages: ChatMessage[]
  workspaceId?: string
  createdAt: string
}

export interface Prompt {
  id: number
  userId: number
  title: string
  content: string
  description?: string
  category: string
  visibility: "PRIVATE" | "ORGANIZATION" | "PUBLIC"
  likeCount: number
  useCount: number
  createdAt: string
  updatedAt: string
}

export type PromptCategory =
  | "GENERAL" | "CODING" | "WRITING" | "ANALYSIS"
  | "EDUCATION" | "MARKETING" | "DESIGN" | "CUSTOMER_SERVICE"

export interface TokenResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
}

export interface ApiResponse<T> {
  success: boolean
  data?: T
  message?: string
}
