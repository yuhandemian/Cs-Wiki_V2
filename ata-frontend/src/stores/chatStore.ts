"use client"
import { create } from "zustand"
import { ChatMessage, LlmProvider } from "@/types"

interface PanelState {
  provider: LlmProvider
  messages: ChatMessage[]
  streaming: string
  loading: boolean
  error: string | null
}

interface ChatStore {
  selectedProviders: LlmProvider[]
  panels: Record<LlmProvider, PanelState>
  inputMessage: string
  conversationId: string | null
  toggleProvider: (provider: LlmProvider) => void
  setInput: (msg: string) => void
  setPanelLoading: (provider: LlmProvider, loading: boolean) => void
  appendStream: (provider: LlmProvider, chunk: string) => void
  commitStream: (provider: LlmProvider) => void
  addMessage: (provider: LlmProvider, role: "user" | "assistant", content: string) => void
  setPanelError: (provider: LlmProvider, error: string | null) => void
  setConversationId: (id: string | null) => void
  reset: () => void
}

const makePanel = (provider: LlmProvider): PanelState => ({
  provider, messages: [], streaming: "", loading: false, error: null
})

const initialPanels = {} as Record<LlmProvider, PanelState>

export const useMultiChatStore = create<ChatStore>((set, get) => ({
  selectedProviders: ["OPENAI", "ANTHROPIC"],
  panels: initialPanels,
  inputMessage: "",
  conversationId: null,

  toggleProvider: (provider) =>
    set((s) => ({
      selectedProviders: s.selectedProviders.includes(provider)
        ? s.selectedProviders.filter((p) => p !== provider)
        : [...s.selectedProviders, provider],
    })),

  setInput: (inputMessage) => set({ inputMessage }),

  setPanelLoading: (provider, loading) =>
    set((s) => ({
      panels: {
        ...s.panels,
        [provider]: { ...(s.panels[provider] ?? makePanel(provider)), loading },
      },
    })),

  appendStream: (provider, chunk) =>
    set((s) => ({
      panels: {
        ...s.panels,
        [provider]: {
          ...(s.panels[provider] ?? makePanel(provider)),
          streaming: (s.panels[provider]?.streaming ?? "") + chunk,
        },
      },
    })),

  commitStream: (provider) =>
    set((s) => {
      const panel = s.panels[provider] ?? makePanel(provider)
      return {
        panels: {
          ...s.panels,
          [provider]: {
            ...panel,
            messages: [
              ...panel.messages,
              { role: "assistant" as const, content: panel.streaming },
            ],
            streaming: "",
            loading: false,
          },
        },
      }
    }),

  addMessage: (provider, role, content) =>
    set((s) => ({
      panels: {
        ...s.panels,
        [provider]: {
          ...(s.panels[provider] ?? makePanel(provider)),
          messages: [
            ...(s.panels[provider]?.messages ?? []),
            { role, content },
          ],
        },
      },
    })),

  setPanelError: (provider, error) =>
    set((s) => ({
      panels: {
        ...s.panels,
        [provider]: { ...(s.panels[provider] ?? makePanel(provider)), error, loading: false },
      },
    })),

  setConversationId: (conversationId) => set({ conversationId }),

  reset: () => set({ panels: initialPanels, inputMessage: "", conversationId: null }),
}))
