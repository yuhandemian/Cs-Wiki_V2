"use client"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { promptApi } from "@/lib/api"
import { useQueryClient } from "@tanstack/react-query"
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter,
} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"

const schema = z.object({
  title: z.string().min(1, "제목을 입력해주세요"),
  content: z.string().min(1, "프롬프트 내용을 입력해주세요"),
  description: z.string().optional(),
  category: z.string().min(1).default("GENERAL"),
  visibility: z.enum(["PRIVATE", "ORGANIZATION", "PUBLIC"]).default("PRIVATE"),
})

type FormData = z.infer<typeof schema>

export function CreatePromptDialog({ open, onOpenChange }: {
  open: boolean; onOpenChange: (v: boolean) => void
}) {
  const qc = useQueryClient()
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema) as any,
    defaultValues: { category: "GENERAL", visibility: "PRIVATE" },
  })

  const onSubmit = async (data: FormData) => {
    await promptApi.create(data)
    qc.invalidateQueries({ queryKey: ["prompts"] })
    reset()
    onOpenChange(false)
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="bg-card border-border max-w-lg">
        <DialogHeader>
          <DialogTitle>새 프롬프트 만들기</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <Input {...register("title")} placeholder="프롬프트 제목" className="bg-input border-border" />
            {errors.title && <p className="text-xs text-destructive mt-1">{errors.title.message}</p>}
          </div>
          <div>
            <Textarea
              {...register("content")}
              placeholder="프롬프트 내용을 입력하세요. {변수}를 사용할 수 있어요."
              className="bg-input border-border min-h-[120px] text-sm"
            />
            {errors.content && <p className="text-xs text-destructive mt-1">{errors.content.message}</p>}
          </div>
          <Input {...register("description")} placeholder="설명 (선택)" className="bg-input border-border" />
          <div className="flex gap-3">
            <div className="flex-1">
              <select {...register("category")} className="w-full h-9 px-3 rounded-md bg-input border border-border text-sm text-foreground">
                <option value="GENERAL">일반</option>
                <option value="CODING">코딩</option>
                <option value="WRITING">글쓰기</option>
                <option value="ANALYSIS">분석</option>
                <option value="EDUCATION">교육</option>
                <option value="MARKETING">마케팅</option>
                <option value="DESIGN">디자인</option>
                <option value="CUSTOMER_SERVICE">고객서비스</option>
              </select>
            </div>
            <div className="flex-1">
              <select {...register("visibility")} className="w-full h-9 px-3 rounded-md bg-input border border-border text-sm text-foreground">
                <option value="PRIVATE">나만 보기</option>
                <option value="ORGANIZATION">조직 공유</option>
                <option value="PUBLIC">전체 공개</option>
              </select>
            </div>
          </div>
          <DialogFooter>
            <Button type="button" variant="ghost" onClick={() => onOpenChange(false)}>취소</Button>
            <Button type="submit" disabled={isSubmitting} className="bg-primary hover:bg-primary/90">
              {isSubmitting ? "저장 중..." : "저장"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
