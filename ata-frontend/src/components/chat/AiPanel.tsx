"use client"
import { PROVIDERS, LlmProvider } from "@/types"
import { useMultiChatStore } from "@/stores/chatStore"
import { ScrollArea } from "@/components/ui/scroll-area"
import { ChatMessageBubble, StreamingBubble } from "./ChatMessage"
import { AlertCircle, Loader2, X } from "lucide-react"
import { useEffect, useRef } from "react"

interface Props {
  provider: LlmProvider
}

export function AiPanel({ provider }: Props) {
  const { panels, toggleProvider } = useMultiChatStore()
  const panel = panels[provider]
  const info = PROVIDERS.find((p) => p.id === provider)!
  const bottomRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" })
  }, [panel?.messages, panel?.streaming])

  return (
    <div className="flex flex-col h-full border border-border rounded-xl overflow-hidden bg-card min-w-0">
      {/* Panel header */}
      <div
        className="flex items-center justify-between px-4 py-3 border-b border-border"
        style={{ borderTopColor: info.color, borderTopWidth: 2 }}
      >
        <div className="flex items-center gap-2">
          <div
            className="w-2.5 h-2.5 rounded-full"
            style={{ backgroundColor: info.color }}
          />
          <span className="font-semibold text-sm">{info.name}</span>
          <span className="text-xs text-muted-foreground">{info.model}</span>
        </div>
        <div className="flex items-center gap-2">
          {panel?.loading && (
            <Loader2 className="w-3.5 h-3.5 text-muted-foreground animate-spin" />
          )}
          <button
            onClick={() => toggleProvider(provider)}
            className="w-5 h-5 flex items-center justify-center rounded hover:bg-accent text-muted-foreground hover:text-foreground transition-colors"
          >
            <X className="w-3 h-3" />
          </button>
        </div>
      </div>

      {/* Messages */}
      <ScrollArea className="flex-1 px-4">
        {(!panel?.messages?.length && !panel?.streaming && !panel?.loading) && (
          <div className="flex flex-col items-center justify-center h-full py-16 text-center">
            <div
              className="w-10 h-10 rounded-xl flex items-center justify-center mb-3"
              style={{ backgroundColor: `${info.color}20` }}
            >
              <span className="text-lg font-bold" style={{ color: info.color }}>
                {info.name[0]}
              </span>
            </div>
            <p className="text-sm text-muted-foreground">
              {info.name}에게 질문해보세요
            </p>
          </div>
        )}

        {panel?.messages?.map((msg, i) => (
          <ChatMessageBubble
            key={i}
            message={msg}
            providerColor={msg.role === "assistant" ? info.color : undefined}
          />
        ))}

        {panel?.streaming && (
          <StreamingBubble content={panel.streaming} providerColor={info.color} />
        )}

        {panel?.loading && !panel?.streaming && (
          <div className="flex gap-3 py-3">
            <div
              className="flex items-center justify-center w-7 h-7 rounded-full shrink-0"
              style={{ backgroundColor: `${info.color}20` }}
            >
              <Loader2 className="w-3.5 h-3.5 animate-spin" style={{ color: info.color }} />
            </div>
            <div className="flex items-center gap-1 px-3.5 py-2.5 rounded-xl bg-secondary/70">
              <span className="w-1.5 h-1.5 rounded-full bg-muted-foreground animate-bounce [animation-delay:0ms]" />
              <span className="w-1.5 h-1.5 rounded-full bg-muted-foreground animate-bounce [animation-delay:150ms]" />
              <span className="w-1.5 h-1.5 rounded-full bg-muted-foreground animate-bounce [animation-delay:300ms]" />
            </div>
          </div>
        )}

        {panel?.error && (
          <div className="flex items-center gap-2 px-3 py-2.5 rounded-lg bg-destructive/10 text-destructive text-sm my-2">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{panel.error}</span>
          </div>
        )}

        <div ref={bottomRef} />
      </ScrollArea>
    </div>
  )
}
