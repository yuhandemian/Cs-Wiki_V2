import { ChatMessage as Msg } from "@/types"
import { cn } from "@/lib/utils"
import { User, Bot } from "lucide-react"

interface Props {
  message: Msg
  streaming?: string
  providerColor?: string
}

export function ChatMessageBubble({ message, streaming, providerColor }: Props) {
  const isUser = message.role === "user"

  return (
    <div className={cn("flex gap-3 py-3", isUser && "flex-row-reverse")}>
      <div
        className={cn(
          "flex items-center justify-center w-7 h-7 rounded-full shrink-0 mt-0.5",
          isUser ? "bg-primary/20" : "bg-secondary"
        )}
        style={!isUser && providerColor ? { backgroundColor: `${providerColor}20` } : undefined}
      >
        {isUser
          ? <User className="w-3.5 h-3.5 text-primary" />
          : <Bot className="w-3.5 h-3.5" style={providerColor ? { color: providerColor } : undefined} />
        }
      </div>
      <div
        className={cn(
          "max-w-[85%] px-3.5 py-2.5 rounded-xl text-sm leading-relaxed whitespace-pre-wrap",
          isUser
            ? "bg-primary/15 text-foreground ml-auto"
            : "bg-secondary/70 text-foreground"
        )}
      >
        {message.content}
        {streaming && (
          <span className="streaming-cursor" />
        )}
      </div>
    </div>
  )
}

export function StreamingBubble({ content, providerColor }: { content: string; providerColor?: string }) {
  if (!content) return null
  return (
    <div className="flex gap-3 py-3">
      <div
        className="flex items-center justify-center w-7 h-7 rounded-full shrink-0 mt-0.5 bg-secondary"
        style={providerColor ? { backgroundColor: `${providerColor}20` } : undefined}
      >
        <Bot className="w-3.5 h-3.5" style={providerColor ? { color: providerColor } : undefined} />
      </div>
      <div className="max-w-[85%] px-3.5 py-2.5 rounded-xl text-sm leading-relaxed bg-secondary/70 text-foreground whitespace-pre-wrap">
        {content}
        <span className="streaming-cursor" />
      </div>
    </div>
  )
}
