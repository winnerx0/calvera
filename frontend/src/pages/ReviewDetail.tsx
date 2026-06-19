import React, { useEffect, useState } from "react"
import { Link, useParams } from "react-router-dom"
import ReactMarkdown from "react-markdown"
import type { Components } from "react-markdown"
import { ArrowLeft, ExternalLink } from "lucide-react"
import { api } from "@/lib/api"
import type { BugFinding, PrReview } from "@/lib/types"
import { SeverityBadge, StatusBadge } from "@/components/StatusBadge"
import { DiffBlock } from "@/components/DiffBlock"
import { Skeleton } from "@/components/ui/skeleton"
import { formatRelative } from "@/lib/time"

const markdownComponents: Components = {
  pre({ children, ...rest }) {
    const child = React.Children.toArray(children)[0]
    if (
      React.isValidElement<{ className?: string; children?: React.ReactNode }>(child) &&
      child.props.className === "language-diff"
    ) {
      const raw = String(child.props.children ?? "").replace(/\n$/, "")
      return <DiffBlock raw={raw} />
    }
    return <pre {...rest}>{children}</pre>
  },
}

function shortSha(sha: string | null): string | null {
  return sha ? sha.slice(0, 7) : null
}

function prUrl(review: PrReview): string {
  return `https://github.com/${review.repositoryFullName}/pull/${review.prNumber}`
}

function Finding({ finding }: { finding: BugFinding }) {
  return (
    <article className="rounded-md border bg-card px-6 py-5">
      <div className="flex items-center gap-3">
        <SeverityBadge severity={finding.severity} />
        <span className="font-mono text-[11px] tracking-wider text-muted-foreground/70">
          {finding.category}
        </span>
        <span className="ml-auto font-mono text-[11.5px] text-muted-foreground tabular">
          {finding.file}:{finding.startLine}
        </span>
      </div>
      <h3 className="mt-3 text-[15px] font-medium">{finding.title}</h3>
      <div className="mt-2 text-[14px] leading-relaxed text-foreground/85 [&_code]:rounded-sm [&_code]:bg-secondary [&_code]:px-1 [&_code]:py-0.5 [&_code]:font-mono [&_code]:text-[13px] [&_p]:my-2">
        <ReactMarkdown components={markdownComponents}>{finding.description}</ReactMarkdown>
      </div>
      {finding.suggestion && (
        <div className="mt-3 [&_pre]:my-0 [&_pre]:overflow-x-auto [&_pre]:rounded-md [&_pre]:border [&_pre]:bg-secondary/60 [&_pre]:p-4">
          <ReactMarkdown components={markdownComponents}>{finding.suggestion}</ReactMarkdown>
        </div>
      )}
    </article>
  )
}

export default function ReviewDetail() {
  const { id } = useParams()
  const [review, setReview] = useState<PrReview | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api
      .get<PrReview>(`/api/reviews/${id}`)
      .then(setReview)
      .catch((e: Error) => setError(e.message))
  }, [id])

  return (
    <div className="rise">
      <Link
        to="/"
        className="mb-10 inline-flex items-center gap-1.5 text-[13px] text-muted-foreground transition-colors hover:text-foreground"
      >
        <ArrowLeft className="size-3.5" />
        All reviews
      </Link>

      {error && (
        <div className="rounded-md border border-signal/25 bg-signal/5 px-4 py-3 text-[13px] text-signal">
          {error}
        </div>
      )}

      {!review && !error && (
        <div className="space-y-4">
          <Skeleton className="h-12 w-2/3" />
          <Skeleton className="h-5 w-1/2" />
          <Skeleton className="mt-8 h-40 w-full" />
        </div>
      )}

      {review && (
        <div className="space-y-12">
          <header className="space-y-6">
            <div className="flex items-center gap-3">
              <span className="font-mono text-[12px] tracking-wider text-muted-foreground tabular">
                #{review.prNumber}
              </span>
              <span className="h-px flex-1 bg-border" />
              <StatusBadge status={review.status} />
            </div>

            <h1 className="display-title">
              {review.prTitle || "Untitled pull request"}
            </h1>

            <p className="stamp">
              <span>{review.repositoryFullName}</span>
              <span className="mx-2 text-muted-foreground/40">·</span>
              <span>{review.action}</span>
              {shortSha(review.headSha) && (
                <>
                  <span className="mx-2 text-muted-foreground/40">·</span>
                  <span>
                    head <span className="text-foreground/80">{shortSha(review.headSha)}</span>
                  </span>
                </>
              )}
              <span className="mx-2 text-muted-foreground/40">·</span>
              <span>{formatRelative(review.createdAt)}</span>
            </p>

            <div className="double-rule" />

            <dl className="grid gap-x-10 gap-y-5 sm:grid-cols-3">
              <div>
                <dt className="micro-label mb-1.5">Delivery</dt>
                <dd className="break-all font-mono text-[12px] text-muted-foreground">
                  {review.deliveryId}
                </dd>
              </div>
              <div>
                <dt className="micro-label mb-1.5">Received</dt>
                <dd className="font-mono text-[12.5px] tabular">
                  {new Date(review.createdAt).toISOString().replace("T", " ").replace(/\.\d+Z$/, " UTC")}
                </dd>
              </div>
              <div>
                <dt className="micro-label mb-1.5">Pull request</dt>
                <dd>
                  <a
                    href={prUrl(review)}
                    target="_blank"
                    rel="noreferrer"
                    className="inline-flex items-center gap-1.5 text-[13px] text-foreground/80 underline-offset-4 transition-colors hover:text-foreground hover:underline"
                  >
                    View on GitHub
                    <ExternalLink className="size-3" />
                  </a>
                </dd>
              </div>
            </dl>
          </header>

          {review.summary && (
            <section className="min-w-0">
              <p className="micro-label mb-4">Summary</p>
              <article className="rounded-md border bg-card px-8 py-7 text-[15px] leading-relaxed [&_a]:underline [&_code]:rounded-sm [&_code]:bg-secondary [&_code]:px-1 [&_code]:py-0.5 [&_code]:font-mono [&_code]:text-[13px] [&_li]:my-1.5 [&_ol]:list-decimal [&_ol]:pl-6 [&_p]:my-3 [&_ul]:list-disc [&_ul]:pl-6">
                <ReactMarkdown components={markdownComponents}>{review.summary}</ReactMarkdown>
              </article>
            </section>
          )}

          <section className="min-w-0">
            <div className="mb-4 flex items-baseline justify-between">
              <p className="micro-label">Findings</p>
              {review.findings.length > 0 && (
                <span className="font-mono text-[10.5px] tracking-wider text-muted-foreground/70">
                  {review.findings.length} {review.findings.length === 1 ? "issue" : "issues"}
                </span>
              )}
            </div>
            {review.findings.length > 0 ? (
              <div className="space-y-4">
                {review.findings.map((finding, i) => (
                  <Finding key={i} finding={finding} />
                ))}
              </div>
            ) : (
              <div className="flex flex-col items-center rounded-md border border-dashed py-24 text-center">
                <StatusBadge status={review.status} />
                <p className="mt-4 max-w-xs text-[14px] text-muted-foreground">
                  {review.status === "FAILED"
                    ? "Analysis didn't complete for this pull request."
                    : review.status === "DONE"
                    ? "No potential bugs were detected in this pull request."
                    : "Waiting for analysis. This page will reflect the result once it's ready."}
                </p>
              </div>
            )}
          </section>
        </div>
      )}
    </div>
  )
}
