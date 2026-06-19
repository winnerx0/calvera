import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import { GitPullRequest, Loader2 } from "lucide-react"
import { api } from "@/lib/api"
import type { Project, PrReview, PullRequest } from "@/lib/types"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Skeleton } from "@/components/ui/skeleton"

export function ReviewPrDialog({ onTriggered }: { onTriggered?: () => void }) {
  const [open, setOpen] = useState(false)
  const [projects, setProjects] = useState<Project[] | null>(null)
  const [projectId, setProjectId] = useState<number | null>(null)
  const [pulls, setPulls] = useState<PullRequest[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [triggering, setTriggering] = useState<number | null>(null)
  const navigate = useNavigate()

  useEffect(() => {
    if (!open || projects) return
    api
      .get<Project[]>("/api/projects")
      .then(setProjects)
      .catch((e: Error) => setError(e.message))
  }, [open, projects])

  useEffect(() => {
    if (projectId == null) return
    setPulls(null)
    setError(null)
    api
      .get<PullRequest[]>(`/api/reviews/pulls?projectId=${projectId}`)
      .then(setPulls)
      .catch((e: Error) => setError(e.message))
  }, [projectId])

  const onOpenChange = (next: boolean) => {
    setOpen(next)
    if (!next) {
      setProjectId(null)
      setPulls(null)
      setError(null)
    }
  }

  const trigger = async (prNumber: number) => {
    if (projectId == null) return
    setTriggering(prNumber)
    setError(null)
    try {
      const review = await api.post<PrReview>("/api/reviews/trigger", { projectId, prNumber })
      onOpenChange(false)
      onTriggered?.()
      navigate(`/reviews/${review.id}`)
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setTriggering(null)
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogTrigger asChild>
        <Button variant="outline" size="sm" className="gap-1.5">
          <GitPullRequest className="size-3.5" />
          Review a PR
        </Button>
      </DialogTrigger>
      <DialogContent className="max-h-[85vh] overflow-hidden sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>Review a pull request</DialogTitle>
          <DialogDescription>
            Pick a project, then choose an open pull request to analyze on demand.
          </DialogDescription>
        </DialogHeader>

        {error && (
          <div className="rounded-md border border-signal/25 bg-signal/5 px-3 py-2 text-[13px] text-signal">
            {error}
          </div>
        )}

        <div className="space-y-4">
          <div>
            <p className="micro-label mb-1.5">Project</p>
            {!projects ? (
              <Skeleton className="h-9 w-full" />
            ) : projects.length === 0 ? (
              <p className="text-[13px] text-muted-foreground">
                No projects yet. Create one first.
              </p>
            ) : (
              <select
                className="h-9 w-full rounded-md border bg-background px-3 text-[13px] outline-none focus:ring-1 focus:ring-ring"
                value={projectId ?? ""}
                onChange={(e) => setProjectId(e.target.value ? Number(e.target.value) : null)}
              >
                <option value="">Select a project…</option>
                {projects.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.name} — {p.repositoryName}
                  </option>
                ))}
              </select>
            )}
          </div>

          {projectId != null && (
            <div>
              <p className="micro-label mb-1.5">Open pull requests</p>
              {!pulls ? (
                <div className="space-y-px">
                  {Array.from({ length: 3 }).map((_, i) => (
                    <Skeleton key={i} className="h-12 rounded-none" />
                  ))}
                </div>
              ) : pulls.length === 0 ? (
                <p className="text-[13px] text-muted-foreground">
                  No open pull requests on this repository.
                </p>
              ) : (
                <ul className="max-h-64 divide-y overflow-y-auto rounded-md border">
                  {pulls.map((pr) => (
                    <li key={pr.number}>
                      <button
                        type="button"
                        disabled={triggering != null}
                        onClick={() => trigger(pr.number)}
                        className={cn(
                          "flex w-full items-center gap-3 px-3 py-2.5 text-left transition-colors hover:bg-secondary/50 disabled:opacity-50"
                        )}
                      >
                        <span className="font-mono text-[11px] text-muted-foreground tabular">
                          #{pr.number}
                        </span>
                        <span className="min-w-0 flex-1">
                          <span className="block truncate text-[13px] font-medium">
                            {pr.title || "Untitled"}
                            {pr.draft && (
                              <span className="ml-2 font-mono text-[10px] uppercase text-muted-foreground">
                                draft
                              </span>
                            )}
                          </span>
                          {pr.author && (
                            <span className="block truncate font-mono text-[11px] text-muted-foreground">
                              {pr.author}
                            </span>
                          )}
                        </span>
                        {triggering === pr.number && (
                          <Loader2 className="size-3.5 animate-spin text-muted-foreground" />
                        )}
                      </button>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  )
}
