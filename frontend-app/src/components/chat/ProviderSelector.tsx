"use client"
import { PROVIDERS } from "@/types"
import { useMultiChatStore } from "@/stores/chatStore"
import { cn } from "@/lib/utils"
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip"
import { Check } from "lucide-react"

export function ProviderSelector() {
  const { selectedProviders, toggleProvider } = useMultiChatStore()

  return (
    <TooltipProvider>
      <div className="flex items-center gap-2 flex-wrap">
        <span className="text-xs text-muted-foreground mr-1">AI 선택</span>
        {PROVIDERS.map((p) => {
          const selected = selectedProviders.includes(p.id)
          return (
            <Tooltip key={p.id}>
              <TooltipTrigger asChild>
                <button
                  onClick={() => toggleProvider(p.id)}
                  className={cn(
                    "flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium border transition-all",
                    selected
                      ? "border-primary bg-primary/15 text-primary"
                      : "border-border bg-secondary/50 text-muted-foreground hover:border-primary/50 hover:text-foreground"
                  )}
                >
                  {selected && <Check className="w-3 h-3" />}
                  {p.name}
                </button>
              </TooltipTrigger>
              <TooltipContent side="top" className="text-xs">
                {p.model}
              </TooltipContent>
            </Tooltip>
          )
        })}
        {selectedProviders.length > 0 && (
          <span className="text-xs text-muted-foreground ml-1">
            {selectedProviders.length}개 선택됨
          </span>
        )}
      </div>
    </TooltipProvider>
  )
}
