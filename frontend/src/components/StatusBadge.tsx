import type { ReviewStatus, Severity } from "@/lib/types"
import { cn } from "@/lib/utils"

const statusConfig: Record<
  ReviewStatus,
  { label: string; tone: string; pulse?: boolean }
> = {
  PENDING: { label: "queued", tone: "text-muted-foreground" },
  ANALYZING: { label: "analyzing", tone: "text-amber", pulse: true },
  DONE: { label: "done", tone: "text-jade" },
  FAILED: { label: "failed", tone: "text-signal" },
}

export function StatusBadge({ status }: { status: ReviewStatus }) {
  const { label, tone, pulse } = statusConfig[status] ?? statusConfig.PENDING
  return (
    <span className={cn("bracket", tone)}>
      {pulse && (
        <span className="h-1 w-1 rounded-full bg-current animate-[pulse-dot_1.2s_ease-in-out_infinite]" />
      )}
      {label}
    </span>
  )
}

const severityTone: Record<Severity, string> = {
  high: "text-signal",
  medium: "text-amber",
  low: "text-muted-foreground",
}

export function SeverityBadge({ severity }: { severity: Severity }) {
  return (
    <span className={cn("bracket", severityTone[severity] ?? "text-muted-foreground")}>
      {severity}
    </span>
  )
}
