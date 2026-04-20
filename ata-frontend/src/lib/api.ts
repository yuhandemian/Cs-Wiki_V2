import axios from "axios"
import { ApiResponse, ChatMessage, MultiChatResult, Prompt, TokenResponse } from "@/types"

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080",
  timeout: 30000,
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken")
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (res) => res,
  async (error) => {
    if (error.response?.status === 401) {
      const refreshToken = localStorage.getItem("refreshToken")
      if (refreshToken) {
        try {
          const res = await axios.post(`${process.env.NEXT_PUBLIC_API_URL}/auth/refresh`, { refreshToken })
          const { accessToken } = res.data.data
          localStorage.setItem("accessToken", accessToken)
          error.config.headers.Authorization = `Bearer ${accessToken}`
          return api.request(error.config)
        } catch {
          localStorage.clear()
          window.location.href = "/sign-in"
        }
      }
    }
    return Promise.reject(error)
  }
)

// Auth
export const authApi = {
  signUp: (data: { email: string; password: string; name: string }) =>
    api.post<ApiResponse<TokenResponse>>("/auth/sign-up", data),
  signIn: (data: { email: string; password: string }) =>
    api.post<ApiResponse<TokenResponse>>("/auth/sign-in", data),
  signOut: (refreshToken: string) =>
    api.post("/auth/sign-out", { refreshToken }),
}

// AI Proxy
export const aiApi = {
  chat: (data: { provider: string; messages: ChatMessage[]; model?: string; temperature?: number }) =>
    api.post<ApiResponse<{ provider: string; content: string }>>("/api/ai/chat", data),
  multiChat: (data: { providers: string[]; messages: ChatMessage[] }) =>
    api.post<ApiResponse<{ results: Record<string, MultiChatResult> }>>("/api/ai/chat/multi", data),
}

// Chat (conversations)
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
