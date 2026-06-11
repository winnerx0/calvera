import { useEffect, useState } from "react"
import { Check, Copy, Eye, EyeOff, RefreshCw } from "lucide-react"
import { api } from "@/lib/api"
import type { Project } from "@/lib/types"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import { Separator } from "@/components/ui/separator"
import { cn } from "@/lib/utils"

function SecretRow({ project }: { project: Project }) {
  const [secret, setSecret] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [visible, setVisible] = useState(false)
  const [copied, setCopied] = useState(false)

  const fetchSecret = async () => {
    setLoading(true)
    setError(null)
    try {
      const s = await api.get<string>(`/api/projects/${project.id}/secret`)
      setSecret(s)
      setVisible(true)
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setLoading(false)
    }
  }

  const copy = async () => {
    if (!secret) return
    await navigator.clipboard.writeText(secret)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <div className="rounded-md border bg-card p-5">
      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0">
          <p className="text-[13.5px] font-medium">{project.name}</p>
          <p className="mt-0.5 font-mono text-[11px] text-muted-foreground">{project.repositoryName}</p>
        </div>
        <span className="micro-label shrink-0 rounded-sm border bg-secondary px-2 py-1">
          Webhook secret
        </span>
      </div>

      <div className="mt-4">
        {!secret && !error && (
          <Button
            size="sm"
            variant="outline"
            className="gap-2 text-[13px]"
            onClick={fetchSecret}
            disabled={loading}
          >
            {loading ? (
              <RefreshCw className="size-3.5 animate-spin" />
            ) : (
              <Eye className="size-3.5" />
            )}
            {loading ? "Fetching…" : "Reveal secret"}
          </Button>
        )}

        {error && (
          <p className="text-[13px] text-signal">{error}</p>
        )}

        {secret && (
          <div className="flex items-center gap-2">
            <div className="flex-1 overflow-hidden rounded-md border bg-secondary/60 px-3 py-2">
              <p
                className={cn(
                  "font-mono text-[12.5px] break-all transition-all",
                  !visible && "select-none blur-sm"
                )}
              >
                {secret}
              </p>
            </div>
            <Button
              size="icon"
              variant="ghost"
              className="size-8 shrink-0 text-muted-foreground hover:text-foreground"
              onClick={() => setVisible((v) => !v)}
            >
              {visible ? <EyeOff className="size-3.5" /> : <Eye className="size-3.5" />}
            </Button>
            <Button
              size="icon"
              variant="ghost"
              className="size-8 shrink-0 text-muted-foreground hover:text-foreground"
              onClick={copy}
            >
              {copied ? <Check className="size-3.5 text-jade" /> : <Copy className="size-3.5" />}
            </Button>
          </div>
        )}
      </div>

      <Separator className="mt-4" />
      <div className="mt-3">
        <p className="micro-label mb-1">Webhook URL</p>
        <p className="font-mono text-[12px] text-muted-foreground break-all">
          {window.location.origin}/webhook/github?projectId={project.id}
        </p>
      </div>
    </div>
  )
}

export default function Settings() {
  const [projects, setProjects] = useState<Project[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api
      .get<Project[]>("/api/projects")
      .then(setProjects)
      .catch((e: Error) => setError(e.message))
  }, [])

  return (
    <div className="rise">
      <div className="mb-8">
        <p className="micro-label mb-2">Account</p>
        <h1 className="text-2xl font-light tracking-tight">Settings</h1>
      </div>

      <section>
        <h2 className="mb-4 text-[13px] font-medium">Webhook secrets</h2>
        <p className="mb-6 max-w-lg text-[13px] text-muted-foreground">
          Add these as the webhook secret in your GitHub repository settings.
          Calvera uses them to verify that payloads come from GitHub.
        </p>

        {error && (
          <div className="rounded-md border border-signal/25 bg-signal/5 px-4 py-3 text-[13px] text-signal">
            {error}
          </div>
        )}

        {!projects && !error && (
          <div className="space-y-3">
            {Array.from({ length: 2 }).map((_, i) => (
              <Skeleton key={i} className="h-36 rounded-md" />
            ))}
          </div>
        )}

        {projects && projects.length === 0 && (
          <div className="flex flex-col items-center rounded-md border border-dashed py-16 text-center">
            <p className="text-[13px] font-medium">No projects yet</p>
            <p className="mt-1 text-[13px] text-muted-foreground">
              Create a project first to generate a webhook secret.
            </p>
          </div>
        )}

        {projects && projects.length > 0 && (
          <div className="space-y-3">
            {projects.map((project) => (
              <SecretRow key={project.id} project={project} />
            ))}
          </div>
        )}
      </section>
    </div>
  )
}
