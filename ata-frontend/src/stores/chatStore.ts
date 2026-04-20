"use client"
import { create } from "zustand"
import { ChatMessage, LlmProvider } from "@/types"

// 한 AI provider 패널의 화면 상태입니다.
// 여러 provider를 동시에 띄우기 위해 provider별로 메시지, 로딩, 스트리밍 텍스트를 따로 관리합니다.
interface PanelState {
  provider: LlmProvider
  messages: ChatMessage[]
  streaming: string
  loading: boolean
  error: string | null
}

// 전체 채팅 화면에서 공유하는 상태와 액션입니다.
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

// provider가 처음 선택되었을 때 기본 패널 상태를 만드는 helper입니다.
const makePanel = (provider: LlmProvider): PanelState => ({
  provider, messages: [], streaming: "", loading: false, error: null
})

const initialPanels = {} as Record<LlmProvider, PanelState>

export const useMultiChatStore = create<ChatStore>((set) => ({
  // 기본으로 GPT와 Claude 패널을 선택해 둡니다.
  selectedProviders: ["OPENAI", "ANTHROPIC"],
  panels: initialPanels,
  inputMessage: "",
  conversationId: null,

  // provider 버튼을 누를 때 선택/해제를 토글합니다.
  toggleProvider: (provider) =>
    set((s) => ({
      selectedProviders: s.selectedProviders.includes(provider)
        ? s.selectedProviders.filter((p) => p !== provider)
        : [...s.selectedProviders, provider],
    })),

  setInput: (inputMessage) => set({ inputMessage }),

  // provider별 요청 시작/종료 상태를 따로 표시합니다.
  setPanelLoading: (provider, loading) =>
    set((s) => ({
      panels: {
        ...s.panels,
        [provider]: { ...(s.panels[provider] ?? makePanel(provider)), loading },
      },
    })),

  // streaming 응답을 받을 때 아직 완성되지 않은 assistant 메시지를 누적합니다.
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

  // streaming이 끝나면 임시 streaming 문자열을 정식 assistant 메시지로 옮깁니다.
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

  // 일반 응답 또는 사용자 입력을 provider 패널의 메시지 목록에 추가합니다.
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

  // 특정 provider 요청만 실패할 수 있으므로 에러도 provider별로 저장합니다.
  setPanelError: (provider, error) =>
    set((s) => ({
      panels: {
        ...s.panels,
        [provider]: { ...(s.panels[provider] ?? makePanel(provider)), error, loading: false },
      },
    })),

  setConversationId: (conversationId) => set({ conversationId }),

  // 새 대화를 시작할 때 화면 상태를 초기화합니다.
  reset: () => set({ panels: initialPanels, inputMessage: "", conversationId: null }),
}))
