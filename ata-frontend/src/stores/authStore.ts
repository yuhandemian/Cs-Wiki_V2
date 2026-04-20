"use client"
import { create } from "zustand"
import { persist } from "zustand/middleware"

// 인증 화면과 레이아웃에서 공유하는 클라이언트 상태입니다.
// accessToken/refreshToken은 Zustand 상태와 localStorage 양쪽에 저장됩니다.
interface AuthState {
  accessToken: string | null
  refreshToken: string | null
  user: { email: string; name: string; plan: string } | null
  setTokens: (access: string, refresh: string) => void
  setUser: (user: AuthState["user"]) => void
  signOut: () => void
  isAuthenticated: () => boolean
}

export const useAuthStore = create<AuthState>()(
  // persist middleware는 Zustand 상태를 localStorage에 저장합니다.
  // 아래 setTokens에서도 직접 localStorage를 쓰는 이유는 api.ts interceptor가 즉시 token을 읽기 때문입니다.
  persist(
    (set, get) => ({
      accessToken: null,
      refreshToken: null,
      user: null,
      setTokens: (accessToken, refreshToken) => {
        // Axios interceptor가 localStorage에서 token을 읽으므로 로그인 직후 바로 저장합니다.
        localStorage.setItem("accessToken", accessToken)
        localStorage.setItem("refreshToken", refreshToken)
        set({ accessToken, refreshToken })
      },
      setUser: (user) => set({ user }),
      signOut: () => {
        // 클라이언트 로그아웃 처리입니다.
        // 서버 sign-out API 호출은 별도로 하고, 여기서는 브라우저에 남은 인증 정보를 지웁니다.
        localStorage.removeItem("accessToken")
        localStorage.removeItem("refreshToken")
        set({ accessToken: null, refreshToken: null, user: null })
      },
      // access token 존재 여부만으로 화면 접근 상태를 판단합니다.
      isAuthenticated: () => !!get().accessToken,
    }),
    { name: "ata-auth" }
  )
)
