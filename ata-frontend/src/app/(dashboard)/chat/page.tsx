"use client"
import { useMultiChatStore } from "@/stores/chatStore"
import { AiPanel } from "@/components/chat/AiPanel"
import { ChatInput } from "@/components/chat/ChatInput"
import { LlmProvider, PROVIDERS } from "@/types"
import { Layers, Sparkles } from "lucide-react"

export default function ChatPage() {
  const { selectedProviders } = useMultiChatStore()

  return (
    <div className="flex flex-col h-full overflow-hidden">
      {/* Header */}
      <div className="flex items-center gap-3 px-6 py-4 border-b border-border bg-card/30">
        <Layers className="w-5 h-5 text-primary" />
        <div>
          <h1 className="font-semibold text-base">멀티 AI 비교</h1>
          <p className="text-xs text-muted-foreground">여러 AI의 답변을 동시에 비교하세요</p>
        </div>
        <div className="ml-auto flex items-center gap-1.5 text-xs text-muted-foreground">
          <Sparkles className="w-3.5 h-3.5 text-primary" />
          <span>{selectedProviders.length}개 AI 선택됨</span>
        </div>
      </div>

      {/* AI Panels Grid */}
      <div className="flex-1 overflow-hidden p-4">
        {selectedProviders.length === 0 ? (
          <EmptyState />
        ) : (
          <div
            className="h-full grid gap-3"
            style={{
              gridTemplateColumns: `repeat(${Math.min(selectedProviders.length, 3)}, 1fr)`,
            }}
          >
            {selectedProviders.map((p: LlmProvider) => (
              <AiPanel key={p} provider={p} />
            ))}
          </div>
        )}
      </div>

      {/* Input */}
      <ChatInput />
    </div>
  )
}

function EmptyState() {
  const providerColors: Record<string, string> = Object.fromEntries(
    PROVIDERS.map((p) => [p.id, p.color])
  )
  return (
    <div className="h-full flex flex-col items-center justify-center gap-6 text-center">
      <div className="flex gap-2">
        {PROVIDERS.slice(0, 3).map((p) => (
          <div
            key={p.id}
            className="w-12 h-12 rounded-2xl flex items-center justify-center text-lg font-bold"
            style={{ backgroundColor: `${p.color}20`, color: p.color }}
          >
            {p.name[0]}
          </div>
        ))}
      </div>
      <div>
        <h2 className="text-xl font-bold mb-2">AI를 선택하세요</h2>
        <p className="text-sm text-muted-foreground max-w-sm">
          아래에서 비교할 AI를 선택하면<br />같은 질문에 대한 각 AI의 답변을 동시에 볼 수 있어요
        </p>
      </div>
    </div>
  )
}
