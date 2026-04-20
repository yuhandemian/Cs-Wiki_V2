"use client"
import { Prompt } from "@/types"
import { Card, CardContent, CardFooter, CardHeader } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Heart, Play, Lock, Globe, Building2 } from "lucide-react"
import { promptApi } from "@/lib/api"
import { useQueryClient } from "@tanstack/react-query"
import { useMultiChatStore } from "@/stores/chatStore"
import { useRouter } from "next/navigation"
import { cn } from "@/lib/utils"

const CATEGORY_LABELS: Record<string, string> = {
  GENERAL: "일반", CODING: "코딩", WRITING: "글쓰기", ANALYSIS: "분석",
  EDUCATION: "교육", MARKETING: "마케팅", DESIGN: "디자인", CUSTOMER_SERVICE: "고객서비스",
}

const CATEGORY_COLORS: Record<string, string> = {
  GENERAL: "bg-slate-500/20 text-slate-300",
  CODING: "bg-blue-500/20 text-blue-300",
  WRITING: "bg-purple-500/20 text-purple-300",
  ANALYSIS: "bg-amber-500/20 text-amber-300",
  EDUCATION: "bg-green-500/20 text-green-300",
  MARKETING: "bg-pink-500/20 text-pink-300",
  DESIGN: "bg-cyan-500/20 text-cyan-300",
  CUSTOMER_SERVICE: "bg-orange-500/20 text-orange-300",
}

interface Props {
  prompt: Prompt
  queryKey: unknown[]
}

export function PromptCard({ prompt, queryKey }: Props) {
  const qc = useQueryClient()
  const router = useRouter()
  const { setInput, selectedProviders } = useMultiChatStore()

  const handleLike = async (e: React.MouseEvent) => {
    e.stopPropagation()
    await promptApi.like(prompt.id)
    qc.invalidateQueries({ queryKey })
  }

  const handleUse = async (e: React.MouseEvent) => {
    e.stopPropagation()
    const res = await promptApi.use(prompt.id)
    if (res.data.data) {
      setInput(res.data.data.content)
      router.push("/chat")
    }
  }

  return (
    <Card className="bg-card border-border hover:border-primary/40 transition-all group cursor-default flex flex-col">
      <CardHeader className="pb-2">
        <div className="flex items-start justify-between gap-2">
          <h3 className="font-semibold text-sm leading-snug line-clamp-2 flex-1">{prompt.title}</h3>
          <VisibilityIcon visibility={prompt.visibility} />
        </div>
        <Badge variant="secondary" className={cn("w-fit text-xs border-0 px-2", CATEGORY_COLORS[prompt.category])}>
          {CATEGORY_LABELS[prompt.category] ?? prompt.category}
        </Badge>
      </CardHeader>
      <CardContent className="pb-3 flex-1">
        <p className="text-xs text-muted-foreground line-clamp-3 leading-relaxed">
          {prompt.description ?? prompt.content}
        </p>
      </CardContent>
      <CardFooter className="pt-0 flex items-center justify-between">
        <div className="flex items-center gap-3 text-xs text-muted-foreground">
          <button onClick={handleLike} className="flex items-center gap-1 hover:text-pink-400 transition-colors">
            <Heart className="w-3.5 h-3.5" />
            {prompt.likeCount}
          </button>
          <span className="flex items-center gap-1">
            <Play className="w-3 h-3" />
            {prompt.useCount}
          </span>
        </div>
        <Button
          size="sm"
          variant="ghost"
          className="text-xs h-7 px-3 bg-primary/10 hover:bg-primary/20 text-primary"
          onClick={handleUse}
        >
          사용하기
        </Button>
      </CardFooter>
    </Card>
  )
}

function VisibilityIcon({ visibility }: { visibility: string }) {
  if (visibility === "PUBLIC") return <Globe className="w-3.5 h-3.5 text-green-400 shrink-0" />
  if (visibility === "ORGANIZATION") return <Building2 className="w-3.5 h-3.5 text-blue-400 shrink-0" />
  return <Lock className="w-3.5 h-3.5 text-muted-foreground shrink-0" />
}
