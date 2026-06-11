import { useEffect, useMemo, useState } from "react"
import { Check, Lock, Pencil, Plus, Search, Trash2, X } from "lucide-react"
import { api } from "@/lib/api"
import type { GithubRepo, Project } from "@/lib/types"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Skeleton } from "@/components/ui/skeleton"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { formatRelative } from "@/lib/time"

export default function Projects() {
  const [projects, setProjects] = useState<Project[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api
      .get<Project[]>("/api/projects")
      .then(setProjects)
      .catch((e: Error) => setError(e.message))
  }, [])
  const [dialogOpen, setDialogOpen] = useState(false)
  const [creating, setCreating] = useState(false)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [editName, setEditName] = useState("")

  const [repos, setRepos] = useState<GithubRepo[] | null>(null)
  const [repoError, setRepoError] = useState<string | null>(null)
  const [search, setSearch] = useState("")
  const [selectedRepo, setSelectedRepo] = useState<GithubRepo | null>(null)
  const [name, setName] = useState("")

  useEffect(() => {
    if (!dialogOpen || repos) return
    api
      .get<GithubRepo[]>("/api/github/repos")
      .then(setRepos)
      .catch((e: Error) => setRepoError(e.message))
  }, [dialogOpen, repos])

  const filteredRepos = useMemo(() => {
    if (!repos) return null
    const q = search.trim().toLowerCase()
    if (!q) return repos
    return repos.filter((r) => r.fullName.toLowerCase().includes(q))
  }, [repos, search])

  const selectRepo = (repo: GithubRepo) => {
    setSelectedRepo(repo)
    setName((prev) => (prev.trim() ? prev : repo.name))
  }

  const create = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedRepo) return
    setCreating(true)
    setError(null)
    try {
      const project = await api.post<Project>("/api/projects", {
        name,
        repositoryName: selectedRepo.fullName,
        repositoryId: selectedRepo.id,
      })
      setProjects((prev) => [project, ...(prev ?? [])])
      setName("")
      setSelectedRepo(null)
      setSearch("")
      setDialogOpen(false)
    } catch (err) {
      setError((err as Error).message)
    } finally {
      setCreating(false)
    }
  }

  const rename = async (id: number) => {
    setError(null)
    try {
      const updated = await api.patch<Project>(`/api/projects/${id}`, { name: editName })
      setProjects((prev) => (prev ?? []).map((p) => (p.id === id ? updated : p)))
      setEditingId(null)
    } catch (err) {
      setError((err as Error).message)
    }
  }

  const remove = async (id: number) => {
    setError(null)
    try {
      await api.delete(`/api/projects/${id}`)
      setProjects((prev) => (prev ?? []).filter((p) => p.id !== id))
    } catch (err) {
      setError((err as Error).message)
    }
  }

  return (
    <div className="rise">
      <div className="mb-8 flex items-end justify-between">
        <div>
          <p className="micro-label mb-2">Configuration</p>
          <h1 className="text-2xl font-light tracking-tight">Projects</h1>
        </div>

        <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
          <DialogTrigger asChild>
            <Button size="sm" className="gap-1.5 text-[13px]">
              <Plus className="size-3.5" />
              New project
            </Button>
          </DialogTrigger>
          <DialogContent className="sm:max-w-lg">
            <DialogHeader>
              <DialogTitle className="font-light tracking-tight">New project</DialogTitle>
              <DialogDescription className="text-[13px]">
                Pick a GitHub repository so Calvera can verify its webhook
                deliveries.
              </DialogDescription>
            </DialogHeader>
            <form onSubmit={create} className="space-y-4">
              <div>
                <label className="micro-label mb-1.5 block">Repository</label>
                <div className="relative mb-2">
                  <Search className="absolute top-1/2 left-2.5 size-3.5 -translate-y-1/2 text-muted-foreground" />
                  <Input
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    placeholder="Search repositories…"
                    className="pl-8 text-[13px]"
                  />
                </div>

                <div className="h-56 overflow-y-auto rounded-md border">
                  {repoError && (
                    <p className="px-3 py-4 text-[13px] text-signal">{repoError}</p>
                  )}
                  {!repos && !repoError && (
                    <div className="space-y-px p-1">
                      {Array.from({ length: 5 }).map((_, i) => (
                        <Skeleton key={i} className="h-10 rounded-sm" />
                      ))}
                    </div>
                  )}
                  {filteredRepos && filteredRepos.length === 0 && (
                    <p className="px-3 py-4 text-[13px] text-muted-foreground">
                      No repositories match “{search}”.
                    </p>
                  )}
                  {filteredRepos && filteredRepos.length > 0 && (
                    <ul className="divide-y">
                      {filteredRepos.map((repo) => {
                        const selected = selectedRepo?.id === repo.id
                        return (
                          <li key={repo.id}>
                            <button
                              type="button"
                              onClick={() => selectRepo(repo)}
                              className={cn(
                                "flex w-full items-center gap-2 px-3 py-2.5 text-left transition-colors",
                                selected ? "bg-secondary" : "hover:bg-secondary/50"
                              )}
                            >
                              <span
                                className={cn(
                                  "h-1.5 w-1.5 shrink-0 rounded-full",
                                  selected ? "bg-foreground" : "bg-border"
                                )}
                              />
                              <span className="min-w-0 flex-1">
                                <span className="block truncate font-mono text-[12.5px]">
                                  {repo.fullName}
                                </span>
                              </span>
                              {repo.isPrivate && (
                                <Lock className="size-3 shrink-0 text-muted-foreground" />
                              )}
                              {selected && <Check className="size-3.5 shrink-0" />}
                            </button>
                          </li>
                        )
                      })}
                    </ul>
                  )}
                </div>
              </div>

              <div>
                <label className="micro-label mb-1.5 block">Project name</label>
                <Input
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder={selectedRepo?.name ?? "My service"}
                />
              </div>

              <DialogFooter className="pt-2">
                <Button
                  type="submit"
                  disabled={creating || !selectedRepo}
                  className="w-full text-[13px]"
                >
                  {creating
                    ? "Creating…"
                    : selectedRepo
                      ? `Create from ${selectedRepo.fullName}`
                      : "Select a repository"}
                </Button>
              </DialogFooter>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      {error && (
        <div className="mb-6 rounded-md border border-signal/25 bg-signal/5 px-4 py-3 text-[13px] text-signal">
          {error}
        </div>
      )}

      {!projects && !error && (
        <div className="space-y-px">
          {Array.from({ length: 3 }).map((_, i) => (
            <Skeleton key={i} className="h-[68px] rounded-none first:rounded-t-md last:rounded-b-md" />
          ))}
        </div>
      )}

      {projects && projects.length === 0 && (
        <div className="flex flex-col items-center rounded-md border border-dashed py-20 text-center">
          <p className="text-[13px] font-medium">No projects yet</p>
          <p className="mt-1 max-w-sm text-[13px] text-muted-foreground">
            Create a project to generate a webhook secret for a repository.
          </p>
        </div>
      )}

      {projects && projects.length > 0 && (
        <ul className="divide-y overflow-hidden rounded-md border bg-card">
          {projects.map((project) => (
            <li
              key={project.id}
              className="flex items-center justify-between gap-4 px-5 py-4"
            >
              <div className="min-w-0">
                {editingId === project.id ? (
                  <div className="flex items-center gap-2">
                    <Input
                      autoFocus
                      value={editName}
                      onChange={(e) => setEditName(e.target.value)}
                      className="h-8 max-w-xs text-[13px]"
                      onKeyDown={(e) => e.key === "Enter" && rename(project.id)}
                    />
                    <Button size="icon" variant="ghost" className="size-8" onClick={() => rename(project.id)}>
                      <Check className="size-3.5" />
                    </Button>
                    <Button size="icon" variant="ghost" className="size-8" onClick={() => setEditingId(null)}>
                      <X className="size-3.5" />
                    </Button>
                  </div>
                ) : (
                  <>
                    <p className="truncate text-[13.5px] font-medium">{project.name}</p>
                    <p className="mt-0.5 truncate font-mono text-[11px] text-muted-foreground">
                      {project.repositoryName} · id {project.repositoryId}
                    </p>
                  </>
                )}
              </div>
              <div className="flex items-center gap-1">
                <span className="mr-3 font-mono text-[11px] text-muted-foreground tabular">
                  {formatRelative(project.createdAt)}
                </span>
                <Button
                  size="icon"
                  variant="ghost"
                  className="size-8 text-muted-foreground hover:text-foreground"
                  onClick={() => {
                    setEditingId(project.id)
                    setEditName(project.name)
                  }}
                >
                  <Pencil className="size-3.5" />
                </Button>
                <Button
                  size="icon"
                  variant="ghost"
                  className="size-8 text-muted-foreground hover:text-signal"
                  onClick={() => remove(project.id)}
                >
                  <Trash2 className="size-3.5" />
                </Button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
