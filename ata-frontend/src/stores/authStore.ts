"use client"
import { create } from "zustand"
import { persist } from "zustand/middleware"

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
  persist(
    (set, get) => ({
      accessToken: null,
      refreshToken: null,
      user: null,
      setTokens: (accessToken, refreshToken) => {
        localStorage.setItem("accessToken", accessToken)
        localStorage.setItem("refreshToken", refreshToken)
        set({ accessToken, refreshToken })
      },
      setUser: (user) => set({ user }),
      signOut: () => {
        localStorage.removeItem("accessToken")
        localStorage.removeItem("refreshToken")
        set({ accessToken: null, refreshToken: null, user: null })
      },
      isAuthenticated: () => !!get().accessToken,
    }),
    { name: "ata-auth" }
  )
)
