import type { CiEventStatus } from "@/lib/types"
import { cn } from "@/lib/utils"

const config: Record<CiEventStatus, { label: string; dot: string; pulse?: boolean }> = {
  PENDING: { label: "Pending", dot: "bg-muted-foreground/60" },
  ANALYZING: { label: "Analyzing", dot: "bg-amber", pulse: true },
  DONE: { label: "Analyzed", dot: "bg-jade" },
  FAILED: { label: "Analysis failed", dot: "bg-signal" },
}

export function StatusBadge({ status }: { status: CiEventStatus }) {
  const { label, dot, pulse } = config[status] ?? config.PENDING
  return (
    <span className="inline-flex items-center gap-1.5">
      <span
        className={cn(
          "h-1.5 w-1.5 rounded-full",
          dot,
          pulse && "animate-[pulse-dot_1.2s_ease-in-out_infinite]"
        )}
      />
      <span className="micro-label text-foreground/70">{label}</span>
    </span>
  )
}

export function ConclusionBadge({ conclusion }: { conclusion: string }) {
  return (
    <span
      className={cn(
        "inline-flex items-center rounded-sm border px-1.5 py-0.5 font-mono text-[11px]",
        conclusion === "failure"
          ? "border-signal/25 bg-signal/5 text-signal"
          : "border-border bg-secondary text-muted-foreground"
      )}
    >
      {conclusion}
    </span>
  )
}
