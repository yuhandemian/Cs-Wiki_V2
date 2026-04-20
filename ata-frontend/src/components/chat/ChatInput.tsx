"use client"
import { useRef, useCallback } from "react"
import { useMultiChatStore } from "@/stores/chatStore"
import { aiApi } from "@/lib/api"
import { LlmProvider } from "@/types"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import { Send, Square } from "lucide-react"
import { ProviderSelector } from "./ProviderSelector"

export function ChatInput() {
  const { inputMessage, setInput, selectedProviders, addMessage, setPanelLoading, setPanelError, appendStream, commitStream, panels } = useMultiChatStore()
  const abortRef = useRef<AbortController | null>(null)
  const isLoading = selectedProviders.some((p) => panels[p]?.loading)

  const submit = useCallback(async () => {
    const text = inputMessage.trim()
    if (!text || !selectedProviders.length || isLoading) return

    setInput("")

    // 각 패널에 유저 메시지 추가
    selectedProviders.forEach((p) => addMessage(p, "user", text))

    // 이전 메시지 수집 (첫 번째 프로바이더 기준)
    const prevMessages = (panels[selectedProviders[0]]?.messages ?? []).slice(0, -1)
    const messages = [...prevMessages, { role: "user" as const, content: text }]

    abortRef.current = new AbortController()

    // 선택된 모든 AI에 동시 요청 (ATA 핵심 기능)
    await Promise.allSettled(
      selectedProviders.map(async (provider: LlmProvider) => {
        setPanelLoading(provider, true)
        try {
          const res = await aiApi.chat({ provider, messages })
          const content = res.data.data?.content ?? ""
          addMessage(provider, "assistant", content)
          setPanelLoading(provider, false)
        } catch (e: unknown) {
          const msg = e instanceof Error ? e.message : "응답 실패"
          setPanelError(provider, msg)
        }
      })
    )
  }, [inputMessage, selectedProviders, isLoading, panels, setInput, addMessage, setPanelLoading, setPanelError, appendStream, commitStream])

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault()
      submit()
    }
  }

  const stop = () => {
    abortRef.current?.abort()
    selectedProviders.forEach((p) => setPanelLoading(p, false))
  }

  return (
    <div className="border-t border-border bg-card/50 backdrop-blur-sm">
      <div className="max-w-full px-4 py-3">
        <ProviderSelector />
        <div className="flex gap-2 mt-3">
          <Textarea
            value={inputMessage}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="모든 AI에게 동시에 질문하세요... (Enter로 전송, Shift+Enter 줄바꿈)"
            className="flex-1 min-h-[52px] max-h-[200px] resize-none bg-input border-border text-sm placeholder:text-muted-foreground focus-visible:ring-primary/50"
            rows={1}
          />
          {isLoading ? (
            <Button
              variant="destructive"
              size="icon"
              className="shrink-0 h-[52px] w-[52px]"
              onClick={stop}
            >
              <Square className="w-4 h-4" />
            </Button>
          ) : (
            <Button
              size="icon"
              className="shrink-0 h-[52px] w-[52px] bg-primary hover:bg-primary/90"
              onClick={submit}
              disabled={!inputMessage.trim() || !selectedProviders.length}
            >
              <Send className="w-4 h-4" />
            </Button>
          )}
        </div>
        <p className="text-xs text-muted-foreground mt-2 text-center">
          {selectedProviders.length}개 AI에게 동시 전송 · Enter로 전송
        </p>
      </div>
    </div>
  )
}
