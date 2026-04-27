import axios from "axios"
import { ApiResponse, ChatMessage, MultiChatResult, Prompt, TokenResponse } from "@/types"

// 프론트엔드의 모든 백엔드 호출이 공유하는 Axios 인스턴스입니다.
// 기본값은 api-gateway 포트인 8080이며, 배포 환경에서는 NEXT_PUBLIC_API_URL로 바꿀 수 있습니다.
const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080",
  timeout: 30000,
})

api.interceptors.request.use((config) => {
  // 로그인 후 localStorage에 저장된 access token을 모든 요청에 자동으로 붙입니다.
  // gateway의 JwtAuthFilter는 이 Authorization 헤더를 읽어 JWT를 검증합니다.
  const token = localStorage.getItem("accessToken")
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (res) => res,
  async (error) => {
    // access token이 만료되면 gateway 또는 service가 401을 반환할 수 있습니다.
    // 이때 refresh token이 있으면 새 access token을 받아 원래 요청을 한 번 재시도합니다.
    if (error.response?.status === 401) {
      const refreshToken = localStorage.getItem("refreshToken")
      if (refreshToken) {
        try {
          // refresh API는 /auth/**라서 gateway에서 JWT 필터 없이 auth-service로 라우팅됩니다.
          const res = await axios.post(`${process.env.NEXT_PUBLIC_API_URL}/auth/refresh`, { refreshToken })
          const { accessToken } = res.data.data
          localStorage.setItem("accessToken", accessToken)
          error.config.headers.Authorization = `Bearer ${accessToken}`
          return api.request(error.config)
        } catch {
          // refresh까지 실패하면 더 이상 인증 상태를 유지할 수 없으므로 로그인 화면으로 보냅니다.
          localStorage.clear()
          window.location.href = "/sign-in"
        }
      }
    }
    return Promise.reject(error)
  }
)

// Auth
// auth-service로 가는 API 묶음입니다.
export const authApi = {
  signUp: (data: { email: string; password: string; name: string }) =>
    api.post<ApiResponse<TokenResponse>>("/auth/sign-up", data),
  signIn: (data: { email: string; password: string }) =>
    api.post<ApiResponse<TokenResponse>>("/auth/sign-in", data),
  signOut: (refreshToken: string) =>
    api.post("/auth/sign-out", { refreshToken }),
}

// AI Proxy
// ai-proxy-service로 가는 API 묶음입니다.
// provider 문자열은 백엔드에서 LlmProvider enum으로 변환됩니다.
export const aiApi = {
  chat: (data: { provider: string; messages: ChatMessage[]; model?: string; temperature?: number }) =>
    api.post<ApiResponse<{ provider: string; content: string }>>("/api/ai/chat", data),
  multiChat: (data: { providers: string[]; messages: ChatMessage[] }) =>
    api.post<ApiResponse<{ results: Record<string, MultiChatResult> }>>("/api/ai/chat/multi", data),
}

// Chat (conversations)
// chat-service의 대화방 API입니다.
// 실제 사용자 식별자는 프론트가 직접 보내지 않고, gateway가 JWT에서 꺼낸 X-User-Id 헤더로 전달합니다.
export const chatApi = {
  list: (page = 0, size = 20) =>
    api.get<ApiResponse<{ id: string; title: string; provider: string; messageCount: number; updatedAt: string }[]>>(
      `/api/chat/conversations?page=${page}&size=${size}`
    ),
  create: (data: { provider: string; model?: string; title?: string }) =>
    api.post<ApiResponse<object>>("/api/chat/conversations", data),
  get: (id: string) =>
    api.get<ApiResponse<object>>(`/api/chat/conversations/${id}`),
  addMessage: (id: string, data: { role: string; content: string; inputTokens?: number; outputTokens?: number }) =>
    api.post(`/api/chat/conversations/${id}/messages`, data),
  delete: (id: string) =>
    api.delete(`/api/chat/conversations/${id}`),
}

// Prompts
// prompt-service의 프롬프트 API입니다.
// 목록 조회, 공개 라이브러리, 검색, 좋아요, 사용 횟수 증가가 이 객체에 모여 있습니다.
export const promptApi = {
  create: (data: Partial<Prompt>) =>
    api.post<ApiResponse<Prompt>>("/api/prompts", data),
  my: (page = 0) =>
    api.get<ApiResponse<Prompt[]>>(`/api/prompts/my?page=${page}`),
  library: (category?: string, page = 0) =>
    api.get<ApiResponse<Prompt[]>>(`/api/prompts/library?page=${page}${category ? `&category=${category}` : ""}`),
  search: (keyword: string, page = 0) =>
    api.get<ApiResponse<Prompt[]>>(`/api/prompts/search?keyword=${keyword}&page=${page}`),
  like: (id: number) =>
    api.post(`/api/prompts/${id}/like`),
  use: (id: number) =>
    api.post<ApiResponse<Prompt>>(`/api/prompts/${id}/use`),
  delete: (id: number) =>
    api.delete(`/api/prompts/${id}`),
}

export default api
