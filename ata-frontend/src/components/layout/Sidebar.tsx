"use client"
import Link from "next/link"
import { usePathname, useRouter } from "next/navigation"
import { useQuery } from "@tanstack/react-query"
import { chatApi } from "@/lib/api"
import { useAuthStore } from "@/stores/authStore"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { ScrollArea } from "@/components/ui/scroll-area"
import { Separator } from "@/components/ui/separator"
import {
  MessageSquarePlus, BookOpen, Settings, LogOut,
  MessageSquare, ChevronRight, Layers,
} from "lucide-react"

export function Sidebar() {
  const pathname = usePathname()
  const router = useRouter()
  const { signOut, user } = useAuthStore()

  const { data: conversations } = useQuery({
    queryKey: ["conversations"],
    queryFn: () => chatApi.list(0, 30).then((r) => r.data.data ?? []),
  })

  const handleSignOut = () => {
    signOut()
    router.push("/sign-in")
  }

  return (
    <aside className="flex flex-col h-full w-[260px] bg-[hsl(222,47%,7%)] border-r border-border">
      {/* Logo */}
      <div className="flex items-center gap-2 px-4 py-4">
        <div className="flex items-center justify-center w-8 h-8 rounded-lg bg-primary/20">
          <Layers className="w-4 h-4 text-primary" />
        </div>
        <span className="font-bold text-lg tracking-tight">ATA</span>
        <span className="text-xs text-muted-foreground font-medium mt-0.5">All That AI</span>
      </div>

      <Separator />

      {/* New Chat */}
      <div className="px-3 py-3">
        <Button
          className="w-full justify-start gap-2 bg-primary/10 hover:bg-primary/20 text-primary border border-primary/30"
          variant="ghost"
          onClick={() => router.push("/chat")}
        >
          <MessageSquarePlus className="w-4 h-4" />
          새 대화
        </Button>
      </div>

      {/* Nav */}
      <nav className="px-3 space-y-1">
        <NavItem href="/chat" icon={<MessageSquare className="w-4 h-4" />} label="채팅" active={pathname.startsWith("/chat")} />
        <NavItem href="/prompts" icon={<BookOpen className="w-4 h-4" />} label="프롬프트 라이브러리" active={pathname.startsWith("/prompts")} />
      </nav>

      <Separator className="my-3" />

      {/* Recent chats */}
      <div className="px-4 mb-2">
        <p className="text-xs font-medium text-muted-foreground uppercase tracking-wider">최근 대화</p>
      </div>
      <ScrollArea className="flex-1 px-3">
        <div className="space-y-0.5">
          {conversations?.map((conv) => (
            <Link
              key={conv.id}
              href={`/chat/${conv.id}`}
              className={cn(
                "flex items-center gap-2 px-3 py-2 rounded-md text-sm hover:bg-accent transition-colors group",
                pathname === `/chat/${conv.id}` && "bg-accent"
              )}
            >
              <MessageSquare className="w-3.5 h-3.5 text-muted-foreground shrink-0" />
              <span className="truncate flex-1">{conv.title}</span>
              <ChevronRight className="w-3 h-3 text-muted-foreground opacity-0 group-hover:opacity-100 shrink-0" />
            </Link>
          ))}
          {!conversations?.length && (
            <p className="text-xs text-muted-foreground px-3 py-2">아직 대화가 없어요</p>
          )}
        </div>
      </ScrollArea>

      <Separator className="mt-auto" />

      {/* Footer */}
      <div className="px-3 py-3 space-y-1">
        <NavItem href="/settings" icon={<Settings className="w-4 h-4" />} label="설정" active={pathname === "/settings"} />
        <button
          onClick={handleSignOut}
          className="flex items-center gap-2 w-full px-3 py-2 rounded-md text-sm text-muted-foreground hover:bg-accent hover:text-foreground transition-colors"
        >
          <LogOut className="w-4 h-4" />
          {user?.name ?? "로그아웃"}
        </button>
      </div>
    </aside>
  )
}

function NavItem({ href, icon, label, active }: {
  href: string; icon: React.ReactNode; label: string; active: boolean
}) {
  return (
    <Link
      href={href}
      className={cn(
        "flex items-center gap-2 px-3 py-2 rounded-md text-sm transition-colors",
        active ? "bg-accent text-foreground" : "text-muted-foreground hover:bg-accent hover:text-foreground"
      )}
    >
      {icon}
      {label}
    </Link>
  )
}
