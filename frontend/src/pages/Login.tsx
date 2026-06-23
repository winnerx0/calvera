import { FaGithub } from "react-icons/fa"
import { Button } from "@/components/ui/button"
import { apiUrl } from "@/lib/api"

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
        <span className="micro-label">PR Review Intelligence</span>
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
            Pull requests, reviewed and explained. Calvera analyzes your
            GitHub PRs and tells you exactly what changed and why it matters.
          </p>

          <Button
            size="lg"
            className="mt-12 h-11 w-full max-w-xs gap-2.5 rounded-md text-[13px] font-medium"
            onClick={() => { window.location.href = apiUrl("/oauth2/authorization/github") }}
          >
            <FaGithub className="size-4" aria-hidden />
            Continue with GitHub
          </Button>

          <div className="mt-16 grid w-full grid-cols-3 divide-x border-y py-4">
            {[
              ["01", "PR review"],
              ["02", "Diff analysis"],
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
          calvera · pull_request · opened | synchronize
        </span>
      </footer>
    </div>
  )
}
