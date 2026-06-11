import { Button } from "@/components/ui/button"

function GithubMark(props: React.SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 16 16" fill="currentColor" aria-hidden {...props}>
      <path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82a7.42 7.42 0 0 1 2-.27c.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.01 8.01 0 0 0 16 8c0-4.42-3.58-8-8-8Z" />
    </svg>
  )
}

export default function Login() {
  return (
    <div className="relative flex min-h-screen flex-col bg-background">
      <div
        aria-hidden
        className="pointer-events-none absolute inset-0"
        style={{
          backgroundImage:
            "linear-gradient(to right, oklch(0.205 0.005 270 / 0.035) 1px, transparent 1px), linear-gradient(to bottom, oklch(0.205 0.005 270 / 0.035) 1px, transparent 1px)",
          backgroundSize: "72px 72px",
          maskImage: "radial-gradient(ellipse 80% 60% at 50% 40%, black, transparent)",
        }}
      />

      <header className="relative flex items-center justify-between px-8 py-6">
        <span className="micro-label text-foreground">Calvera</span>
        <span className="micro-label">CI Failure Intelligence</span>
      </header>

      <main className="relative flex flex-1 flex-col items-center justify-center px-6 pb-24">
        <div className="rise flex w-full max-w-md flex-col items-center text-center">
          <div className="mb-10 flex items-center gap-2">
            <span className="h-1.5 w-1.5 rounded-full bg-signal" />
            <span className="micro-label">Signal Detected</span>
          </div>

          <h1 className="font-sans text-6xl font-light tracking-tight text-foreground">
            calvera
          </h1>
          <p className="mt-5 max-w-sm text-[15px] leading-relaxed text-muted-foreground">
            Failed CI runs, captured and explained. Calvera listens to your
            GitHub workflows and tells you exactly why they broke.
          </p>

          <Button
            asChild
            size="lg"
            className="mt-12 h-11 w-full max-w-xs gap-2.5 rounded-md text-[13px] font-medium"
          >
            <a href="/oauth/login/github">
              <GithubMark className="size-4" />
              Continue with GitHub
            </a>
          </Button>

          <div className="mt-16 grid w-full grid-cols-3 divide-x border-y py-4">
            {[
              ["01", "Webhook ingest"],
              ["02", "Log analysis"],
              ["03", "Root cause"],
            ].map(([num, label]) => (
              <div key={num} className="flex flex-col items-center gap-1 px-2">
                <span className="font-mono text-[11px] text-muted-foreground/60">{num}</span>
                <span className="micro-label text-foreground/70">{label}</span>
              </div>
            ))}
          </div>
        </div>
      </main>

      <footer className="relative px-8 py-6">
        <span className="font-mono text-[11px] text-muted-foreground/50">
          calvera · workflow_run · failure | cancelled
        </span>
      </footer>
    </div>
  )
}
