import { useEffect, useState } from "react"
import { Link, useParams } from "react-router-dom"
import ReactMarkdown from "react-markdown"
import { ArrowLeft, ExternalLink } from "lucide-react"
import { api } from "@/lib/api"
import type { CiEvent } from "@/lib/types"
import { ConclusionBadge, StatusBadge } from "@/components/StatusBadge"
import { Skeleton } from "@/components/ui/skeleton"
import { formatFull } from "@/lib/time"

export default function EventDetail() {
  const { id } = useParams()
  const [event, setEvent] = useState<CiEvent | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api
      .get<CiEvent>(`/api/events/${id}`)
      .then(setEvent)
      .catch((e: Error) => setError(e.message))
  }, [id])

  return (
    <div className="rise">
      <Link
        to="/"
        className="mb-8 inline-flex items-center gap-1.5 text-[13px] text-muted-foreground transition-colors hover:text-foreground"
      >
        <ArrowLeft className="size-3.5" />
        All events
      </Link>

      {error && (
        <div className="rounded-md border border-signal/25 bg-signal/5 px-4 py-3 text-[13px] text-signal">
          {error}
        </div>
      )}

      {!event && !error && (
        <div className="space-y-4">
          <Skeleton className="h-9 w-1/2" />
          <Skeleton className="h-40 w-full" />
        </div>
      )}

      {event && (
        <div className="grid gap-10 lg:grid-cols-[260px_1fr]">
          <aside className="space-y-6">
            <div>
              <p className="micro-label mb-2">Workflow</p>
              <h1 className="text-xl font-light tracking-tight">
                {event.workflowName || "Unnamed workflow"}
              </h1>
            </div>

            <dl className="space-y-4 border-t pt-5">
              {[
                ["Repository", <span className="font-mono text-[12px]">{event.repositoryFullName}</span>],
                ["Conclusion", <ConclusionBadge conclusion={event.conclusion} />],
                ["Status", <StatusBadge status={event.status} />],
                ["Received", <span className="font-mono text-[12px] tabular">{formatFull(event.createdAt)}</span>],
                ["Delivery", <span className="break-all font-mono text-[11px] text-muted-foreground">{event.deliveryId}</span>],
              ].map(([label, value], i) => (
                <div key={i}>
                  <dt className="micro-label mb-1">{label}</dt>
                  <dd>{value}</dd>
                </div>
              ))}
            </dl>

            {event.jobsUrl && (
              <a
                href={event.jobsUrl}
                target="_blank"
                rel="noreferrer"
                className="inline-flex items-center gap-1.5 border-t pt-5 text-[13px] text-muted-foreground transition-colors hover:text-foreground"
              >
                Raw logs
                <ExternalLink className="size-3" />
              </a>
            )}
          </aside>

          <section className="min-w-0">
            <p className="micro-label mb-3">Analysis</p>
            {event.analysisResult ? (
              <article className="rounded-md border bg-card px-7 py-6 leading-relaxed [&_a]:underline [&_code]:rounded-sm [&_code]:bg-secondary [&_code]:px-1 [&_code]:py-0.5 [&_code]:font-mono [&_code]:text-[12px] [&_h1]:mt-5 [&_h1]:mb-2 [&_h1]:text-lg [&_h1]:font-medium [&_h2]:mt-5 [&_h2]:mb-2 [&_h2]:text-base [&_h2]:font-medium [&_h3]:mt-4 [&_h3]:mb-1.5 [&_h3]:text-[14px] [&_h3]:font-medium [&_li]:my-1 [&_ol]:list-decimal [&_ol]:pl-5 [&_p]:my-2.5 [&_pre]:my-3 [&_pre]:overflow-x-auto [&_pre]:rounded-md [&_pre]:border [&_pre]:bg-secondary/60 [&_pre]:p-4 [&_pre_code]:bg-transparent [&_pre_code]:p-0 [&_ul]:list-disc [&_ul]:pl-5">
                <ReactMarkdown>{event.analysisResult}</ReactMarkdown>
              </article>
            ) : (
              <div className="flex flex-col items-center rounded-md border border-dashed py-16 text-center">
                <StatusBadge status={event.status} />
                <p className="mt-3 max-w-xs text-[13px] text-muted-foreground">
                  {event.status === "FAILED"
                    ? "Analysis failed for this run."
                    : "Analysis hasn't completed yet. Check back shortly."}
                </p>
              </div>
            )}
          </section>
        </div>
      )}
    </div>
  )
}
