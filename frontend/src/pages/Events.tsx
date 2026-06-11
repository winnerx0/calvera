import { useEffect, useState } from "react"
import { Link } from "react-router-dom"
import { ArrowUpRight, Inbox } from "lucide-react"
import { api } from "@/lib/api"
import type { CiEvent } from "@/lib/types"
import { ConclusionBadge, StatusBadge } from "@/components/StatusBadge"
import { Skeleton } from "@/components/ui/skeleton"
import { formatRelative } from "@/lib/time"

export default function Events() {
  const [events, setEvents] = useState<CiEvent[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api
      .get<CiEvent[]>("/api/events")
      .then(setEvents)
      .catch((e: Error) => setError(e.message))
  }, [])

  return (
    <div className="rise">
      <div className="mb-8 flex items-end justify-between">
        <div>
          <p className="micro-label mb-2">Feed</p>
          <h1 className="text-2xl font-light tracking-tight">CI Events</h1>
        </div>
        {events && (
          <span className="font-mono text-[12px] text-muted-foreground tabular">
            {events.length} {events.length === 1 ? "event" : "events"}
          </span>
        )}
      </div>

      {error && (
        <div className="rounded-md border border-signal/25 bg-signal/5 px-4 py-3 text-[13px] text-signal">
          {error}
        </div>
      )}

      {!events && !error && (
        <div className="space-y-px">
          {Array.from({ length: 5 }).map((_, i) => (
            <Skeleton key={i} className="h-[72px] rounded-none first:rounded-t-md last:rounded-b-md" />
          ))}
        </div>
      )}

      {events && events.length === 0 && (
        <div className="flex flex-col items-center rounded-md border border-dashed py-20 text-center">
          <Inbox className="size-5 text-muted-foreground/50" />
          <p className="mt-4 text-[13px] font-medium">No events yet</p>
          <p className="mt-1 max-w-xs text-[13px] text-muted-foreground">
            Failed workflow runs will appear here once your GitHub webhook is
            wired to a project.
          </p>
        </div>
      )}

      {events && events.length > 0 && (
        <div className="overflow-hidden rounded-md border bg-card">
          <div className="grid grid-cols-[1fr_auto_auto] items-center gap-4 border-b bg-secondary/50 px-5 py-2.5 md:grid-cols-[2fr_1.4fr_auto_auto_auto]">
            <span className="micro-label">Workflow</span>
            <span className="micro-label hidden md:block">Repository</span>
            <span className="micro-label hidden w-20 md:block">Run</span>
            <span className="micro-label w-28">Status</span>
            <span className="micro-label w-16 text-right">When</span>
          </div>
          <ul className="divide-y">
            {events.map((event, i) => (
              <li key={event.id} className="rise" style={{ animationDelay: `${i * 35}ms` }}>
                <Link
                  to={`/events/${event.id}`}
                  className="group grid grid-cols-[1fr_auto_auto] items-center gap-4 px-5 py-4 transition-colors hover:bg-secondary/40 md:grid-cols-[2fr_1.4fr_auto_auto_auto]"
                >
                  <div className="min-w-0">
                    <p className="truncate text-[13.5px] font-medium">
                      {event.workflowName || "Unnamed workflow"}
                    </p>
                    <p className="mt-0.5 truncate font-mono text-[11px] text-muted-foreground md:hidden">
                      {event.repositoryFullName}
                    </p>
                  </div>
                  <span className="hidden truncate font-mono text-[12px] text-muted-foreground md:block">
                    {event.repositoryFullName}
                  </span>
                  <span className="hidden w-20 md:block">
                    <ConclusionBadge conclusion={event.conclusion} />
                  </span>
                  <span className="w-28">
                    <StatusBadge status={event.status} />
                  </span>
                  <span className="flex w-16 items-center justify-end gap-1 font-mono text-[11px] text-muted-foreground tabular">
                    {formatRelative(event.createdAt)}
                    <ArrowUpRight className="size-3 opacity-0 transition-opacity group-hover:opacity-100" />
                  </span>
                </Link>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  )
}
