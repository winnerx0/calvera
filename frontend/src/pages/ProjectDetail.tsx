import { useEffect, useState } from "react"
import { Link, useParams } from "react-router-dom"
import { ArrowLeft, ArrowUpRight, Inbox } from "lucide-react"
import { api } from "@/lib/api"
import type { BugFinding, PrReview, Project } from "@/lib/types"
import { Skeleton } from "@/components/ui/skeleton"
import { StatusBadge } from "@/components/StatusBadge"
import { ReviewPrDialog } from "@/components/ReviewPrDialog"
import { formatRelative } from "@/lib/time"

function severitySummary(findings: BugFinding[]): string {
  if (!findings || findings.length === 0) return "no issues"
  const high = findings.filter((f) => f.severity === "high").length
  const med = findings.filter((f) => f.severity === "medium").length
  const low = findings.filter((f) => f.severity === "low").length
  return [high && `${high} high`, med && `${med} med`, low && `${low} low`]
    .filter(Boolean)
    .join(" · ")
}

export default function ProjectDetail() {
  const { id } = useParams<{ id: string }>()
  const projectId = Number(id)

  const [project, setProject] = useState<Project | null>(null)
  const [reviews, setReviews] = useState<PrReview[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  const loadReviews = () => {
    api
      .get<PrReview[]>(`/api/reviews?projectId=${projectId}`)
      .then(setReviews)
      .catch((e: Error) => setError(e.message))
  }

  useEffect(() => {
    if (!projectId) return
    api
      .get<Project>(`/api/projects/${projectId}`)
      .then(setProject)
      .catch((e: Error) => setError(e.message))
    loadReviews()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [projectId])

  return (
    <div className="rise space-y-10">
      <div>
        <Link
          to="/projects"
          className="micro-label inline-flex items-center gap-1 text-muted-foreground hover:text-foreground"
        >
          <ArrowLeft className="size-3" /> Projects
        </Link>
        <div className="mt-2 flex items-end justify-between">
          <div className="min-w-0">
            {project ? (
              <>
                <h1 className="truncate text-2xl font-light tracking-tight">
                  {project.name}
                </h1>
                <p className="mt-1 truncate font-mono text-[12px] text-muted-foreground">
                  {project.repositoryName} · id {project.repositoryId}
                </p>
              </>
            ) : (
              <Skeleton className="h-8 w-64" />
            )}
          </div>
        </div>
      </div>

      {error && (
        <div className="rounded-md border border-signal/25 bg-signal/5 px-4 py-3 text-[13px] text-signal">
          {error}
        </div>
      )}

      <section>
        <div className="mb-3 flex items-end justify-between">
          <div>
            <p className="micro-label mb-1">Section</p>
            <h2 className="text-lg font-light tracking-tight">PR reviews</h2>
          </div>
          <div className="flex items-center gap-4">
            {reviews && (
              <span className="font-mono text-[12px] text-muted-foreground tabular">
                {reviews.length} {reviews.length === 1 ? "review" : "reviews"}
              </span>
            )}
            <ReviewPrDialog onTriggered={loadReviews} />
          </div>
        </div>

        {!reviews && !error && (
          <div className="space-y-px">
            {Array.from({ length: 4 }).map((_, i) => (
              <Skeleton
                key={i}
                className="h-[64px] rounded-none first:rounded-t-md last:rounded-b-md"
              />
            ))}
          </div>
        )}

        {reviews && reviews.length === 0 && (
          <div className="flex flex-col items-center rounded-md border border-dashed py-16 text-center">
            <Inbox className="size-5 text-muted-foreground/50" />
            <p className="mt-4 text-[13px] font-medium">No reviews yet</p>
            <p className="mt-1 max-w-xs text-[13px] text-muted-foreground">
              Trigger a review on an open pull request to populate this list.
            </p>
          </div>
        )}

        {reviews && reviews.length > 0 && (
          <div className="overflow-hidden rounded-md border bg-card">
            <ul className="divide-y">
              {reviews.map((review) => (
                <li key={review.id}>
                  <Link
                    to={`/reviews/${review.id}`}
                    className="group grid grid-cols-[auto_1fr_auto_auto] items-center gap-4 px-5 py-4 transition-colors hover:bg-secondary/40"
                  >
                    <span className="w-12 font-mono text-[11.5px] tracking-wider text-muted-foreground tabular">
                      #{review.prNumber}
                    </span>
                    <div className="min-w-0">
                      <p className="truncate text-[13.5px] font-medium">
                        {review.prTitle || "Untitled pull request"}
                      </p>
                      <p className="mt-0.5 truncate font-mono text-[11px] text-muted-foreground">
                        {severitySummary(review.findings)}
                      </p>
                    </div>
                    <span className="w-28">
                      <StatusBadge status={review.status} />
                    </span>
                    <span className="flex w-16 items-center justify-end gap-1 font-mono text-[11px] text-muted-foreground tabular">
                      {formatRelative(review.createdAt)}
                      <ArrowUpRight className="size-3 opacity-0 transition-opacity group-hover:opacity-100" />
                    </span>
                  </Link>
                </li>
              ))}
            </ul>
          </div>
        )}
      </section>
    </div>
  )
}
