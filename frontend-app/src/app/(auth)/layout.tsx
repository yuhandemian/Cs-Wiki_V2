export default function AuthLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen flex items-center justify-center bg-background p-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center gap-2 mb-2">
            <div className="w-9 h-9 rounded-xl bg-primary/20 flex items-center justify-center">
              <span className="text-primary font-bold text-lg">A</span>
            </div>
            <span className="font-bold text-2xl">ATA</span>
          </div>
          <p className="text-sm text-muted-foreground">여러 AI의 답변을 동시에</p>
        </div>
        {children}
      </div>
    </div>
  )
}
