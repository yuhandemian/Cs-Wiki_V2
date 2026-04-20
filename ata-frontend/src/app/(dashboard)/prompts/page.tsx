"use client"
import { useState } from "react"
import { useQuery } from "@tanstack/react-query"
import { promptApi } from "@/lib/api"
import { PromptCard } from "@/components/prompts/PromptCard"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Search, BookOpen, Plus } from "lucide-react"
import { cn } from "@/lib/utils"
import { CreatePromptDialog } from "@/components/prompts/CreatePromptDialog"

const CATEGORIES = [
  { id: undefined, label: "전체" },
  { id: "CODING", label: "코딩" },
  { id: "WRITING", label: "글쓰기" },
  { id: "ANALYSIS", label: "분석" },
  { id: "MARKETING", label: "마케팅" },
  { id: "EDUCATION", label: "교육" },
  { id: "DESIGN", label: "디자인" },
  { id: "GENERAL", label: "일반" },
]

export default function PromptsPage() {
  const [category, setCategory] = useState<string | undefined>(undefined)
  const [keyword, setKeyword] = useState("")
  const [search, setSearch] = useState("")
  const [tab, setTab] = useState<"library" | "my">("library")
  const [createOpen, setCreateOpen] = useState(false)

  const queryKey = ["prompts", tab, category, search]

  const { data: prompts = [], isLoading } = useQuery({
    queryKey,
    queryFn: () => {
      if (tab === "my") return promptApi.my().then((r) => r.data.data ?? [])
      if (search) return promptApi.search(search).then((r) => r.data.data ?? [])
      return promptApi.library(category).then((r) => r.data.data ?? [])
    },
  })

  return (
    <div className="flex flex-col h-full overflow-hidden">
      {/* Header */}
      <div className="px-6 py-4 border-b border-border bg-card/30">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <BookOpen className="w-5 h-5 text-primary" />
            <h1 className="font-semibold text-base">프롬프트 라이브러리</h1>
          </div>
          <Button
            size="sm"
            className="gap-1.5 bg-primary hover:bg-primary/90"
            onClick={() => setCreateOpen(true)}
          >
            <Plus className="w-3.5 h-3.5" />
            새 프롬프트
          </Button>
        </div>

        {/* Tabs */}
        <div className="flex items-center gap-1 mb-4">
          {(["library", "my"] as const).map((t) => (
            <button
              key={t}
              onClick={() => setTab(t)}
              className={cn(
                "px-4 py-1.5 rounded-full text-sm font-medium transition-colors",
                tab === t
                  ? "bg-primary text-primary-foreground"
                  : "text-muted-foreground hover:text-foreground hover:bg-accent"
              )}
            >
              {t === "library" ? "공개 라이브러리" : "내 프롬프트"}
            </button>
          ))}
        </div>

        {/* Search + Category filter */}
        <div className="flex gap-2 flex-wrap">
          <div className="relative flex-1 min-w-[200px]">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-muted-foreground" />
            <Input
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && setSearch(keyword)}
              placeholder="프롬프트 검색..."
              className="pl-9 bg-input border-border text-sm h-9"
            />
          </div>
          {tab === "library" && (
            <div className="flex gap-1 flex-wrap">
              {CATEGORIES.map((c) => (
                <button
                  key={c.label}
                  onClick={() => setCategory(c.id)}
                  className={cn(
                    "px-3 py-1 rounded-full text-xs border transition-colors",
                    category === c.id
                      ? "bg-primary/15 border-primary/50 text-primary"
                      : "border-border text-muted-foreground hover:border-primary/30 hover:text-foreground"
                  )}
                >
                  {c.label}
                </button>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Grid */}
      <div className="flex-1 overflow-auto p-6">
        {isLoading ? (
          <div className="grid grid-cols-[repeat(auto-fill,minmax(280px,1fr))] gap-4">
            {Array.from({ length: 6 }).map((_, i) => (
              <div key={i} className="h-44 rounded-xl bg-card animate-pulse" />
            ))}
          </div>
        ) : prompts.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-full gap-3 text-center">
            <BookOpen className="w-10 h-10 text-muted-foreground/40" />
            <p className="text-muted-foreground text-sm">
              {tab === "my" ? "아직 만든 프롬프트가 없어요" : "프롬프트를 찾을 수 없어요"}
            </p>
            {tab === "my" && (
              <Button size="sm" onClick={() => setCreateOpen(true)} className="gap-1.5">
                <Plus className="w-3.5 h-3.5" />
                첫 프롬프트 만들기
              </Button>
            )}
          </div>
        ) : (
          <div className="grid grid-cols-[repeat(auto-fill,minmax(280px,1fr))] gap-4">
            {prompts.map((p) => (
              <PromptCard key={p.id} prompt={p} queryKey={queryKey} />
            ))}
          </div>
        )}
      </div>

      <CreatePromptDialog open={createOpen} onOpenChange={setCreateOpen} />
    </div>
  )
}
