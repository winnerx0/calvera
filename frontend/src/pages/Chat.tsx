import { useEffect, useMemo, useRef, useState } from "react"
import { ArrowUp, Loader2, Plus, Search, X } from "lucide-react"
import { api, auth } from "@/lib/api"
import type { PrReview } from "@/lib/types"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Skeleton } from "@/components/ui/skeleton"
import { StatusBadge } from "@/components/StatusBadge"
import { cn } from "@/lib/utils"

type Message = { role: "user" | "assistant"; content: string }

export default function Chat() {
  const [reviews, setReviews] = useState<PrReview[] | null>(null)
  const [reviewsError, setReviewsError] = useState<string | null>(null)
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [threads, setThreads] = useState<Record<number, Message[]>>({})
  const [question, setQuestion] = useState("")
  const [streaming, setStreaming] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [pickerOpen, setPickerOpen] = useState(false)
  const [search, setSearch] = useState("")
  const abortRef = useRef<AbortController | null>(null)
  const scrollRef = useRef<HTMLDivElement | null>(null)
  const pickerRef = useRef<HTMLDivElement | null>(null)
  const searchRef = useRef<HTMLInputElement | null>(null)

  useEffect(() => {
    api
      .get<PrReview[]>("/api/reviews")
      .then(setReviews)
      .catch((e: Error) => setReviewsError(e.message))
  }, [])

  useEffect(() => () => abortRef.current?.abort(), [])

  useEffect(() => {
    if (!pickerOpen) return
    const onDown = (e: MouseEvent) => {
      if (!pickerRef.current?.contains(e.target as Node)) setPickerOpen(false)
    }
    const onKey = (e: KeyboardEvent) => e.key === "Escape" && setPickerOpen(false)
    document.addEventListener("mousedown", onDown)
    document.addEventListener("keydown", onKey)
    queueMicrotask(() => searchRef.current?.focus())
    return () => {
      document.removeEventListener("mousedown", onDown)
      document.removeEventListener("keydown", onKey)
    }
  }, [pickerOpen])

  const selected = useMemo(
    () => reviews?.find((r) => r.id === selectedId) ?? null,
    [reviews, selectedId]
  )
  const thread = selectedId != null ? threads[selectedId] ?? [] : []

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight })
  }, [thread, streaming])

  const filteredReviews = useMemo(() => {
    if (!reviews) return null
    const q = search.trim().toLowerCase()
    if (!q) return reviews
    return reviews.filter(
      (r) =>
        r.repositoryFullName.toLowerCase().includes(q) ||
        (r.prTitle ?? "").toLowerCase().includes(q) ||
        String(r.prNumber).includes(q)
    )
  }, [reviews, search])

  const pickReview = (id: number) => {
    setSelectedId(id)
    setPickerOpen(false)
    setSearch("")
  }

  const append = (id: number, fn: (m: Message[]) => Message[]) =>
    setThreads((prev) => ({ ...prev, [id]: fn(prev[id] ?? []) }))

  const send = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedId || !question.trim() || streaming) return
    const id = selectedId
    const q = question.trim()
    setQuestion("")
    setError(null)
    append(id, (msgs) => [
      ...msgs,
      { role: "user", content: q },
      { role: "assistant", content: "" },
    ])
    setStreaming(true)

    const controller = new AbortController()
    abortRef.current = controller

    try {
      const url = `/api/reviews/${id}/chat/stream?question=${encodeURIComponent(q)}`
      const res = await fetch(url, {
        headers: { Authorization: `Bearer ${auth.accessToken ?? ""}` },
        signal: controller.signal,
      })
      if (!res.ok || !res.body) throw new Error(`Stream failed (${res.status})`)
      const reader = res.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ""

      while (true) {
        const { value, done } = await reader.read()
        if (done) break
        buffer += decoder.decode(value, { stream: true })
        let idx
        while ((idx = buffer.indexOf("\n\n")) !== -1) {
          const block = buffer.slice(0, idx)
          buffer = buffer.slice(idx + 2)
          const { event, data } = parseSseBlock(block)
          if (event === "token") {
            let token = data
            try {
              token = JSON.parse(data) as string
            } catch {
              // fall back to raw data
            }
            append(id, (msgs) => {
              const next = msgs.slice()
              const last = next[next.length - 1]
              if (last && last.role === "assistant") {
                next[next.length - 1] = { ...last, content: last.content + token }
              }
              return next
            })
          } else if (event === "error") {
            setError(data || "Stream error")
          }
        }
      }
    } catch (err) {
      if ((err as Error).name !== "AbortError") setError((err as Error).message)
    } finally {
      setStreaming(false)
      abortRef.current = null
    }
  }

  return (
    <div className="rise mx-auto flex h-[calc(100dvh-11rem)] min-h-[28rem] max-w-2xl flex-col">
      <div ref={scrollRef} className="min-h-0 flex-1 overflow-y-auto pr-1">
        {!selected && thread.length === 0 && (
          <div className="flex h-full flex-col items-start justify-center">
            <p className="micro-label mb-3">Chat</p>
            <h1 className="display-title">Pick a review to begin.</h1>
            <p className="mt-4 max-w-md text-[13.5px] text-muted-foreground">
              Open a pull request's review and ask anything about its diff — why
              a change was made, where a bug might live, what to test.
            </p>
          </div>
        )}

        {selected && thread.length === 0 && (
          <p className="mt-16 text-center text-[13px] text-muted-foreground">
            Ask the first question below.
          </p>
        )}

        {thread.length > 0 && (
          <ul className="divide-y">
            {thread.map((m, i) => {
              const empty = !m.content
              const last = i === thread.length - 1
              return (
                <li key={i} className="rise py-5 first:pt-2">
                  <p className="micro-label mb-2">
                    {m.role === "user" ? "you" : "reviewer"}
                  </p>
                  {empty && m.role === "assistant" && streaming && last ? (
                    <span className="inline-flex items-center gap-2 text-[13px] text-muted-foreground">
                      <Loader2 className="size-3 animate-spin" /> thinking…
                    </span>
                  ) : (
                    <p
                      className={cn(
                        "max-w-[36rem] text-[14px] leading-relaxed break-words whitespace-pre-wrap",
                        m.role === "user" ? "text-muted-foreground" : "text-foreground"
                      )}
                    >
                      {m.content}
                    </p>
                  )}
                </li>
              )
            })}
          </ul>
        )}
      </div>

      {error && <p className="mt-2 text-[12.5px] text-signal">{error}</p>}

      <form onSubmit={send} className="mt-4 space-y-2">
        {selected && (
          <div className="flex items-center justify-between gap-2 px-1">
            <span className="stamp truncate">
              {selected.repositoryFullName} · #{selected.prNumber}
              {selected.prTitle ? ` — ${selected.prTitle}` : ""}
            </span>
            <button
              type="button"
              onClick={() => setSelectedId(null)}
              className="text-muted-foreground hover:text-foreground"
              aria-label="Clear selected review"
            >
              <X className="size-3.5" />
            </button>
          </div>
        )}

        <div ref={pickerRef} className="relative">
          {pickerOpen && (
            <div
              role="listbox"
              className="rise absolute right-0 bottom-[calc(100%+0.5rem)] left-0 z-20 overflow-hidden rounded-md border bg-popover shadow-md"
            >
              <div className="border-b px-2 py-2">
                <div className="relative">
                  <Search className="absolute top-1/2 left-2.5 size-3.5 -translate-y-1/2 text-muted-foreground" />
                  <Input
                    ref={searchRef}
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    placeholder="Search by repo, title, or #number…"
                    className="h-8 pl-8 text-[13px]"
                  />
                </div>
              </div>
              <div className="max-h-72 overflow-y-auto">
                {reviewsError && (
                  <p className="px-4 py-6 text-[13px] text-signal">{reviewsError}</p>
                )}
                {!reviews && !reviewsError && (
                  <div className="space-y-px p-2">
                    {Array.from({ length: 4 }).map((_, i) => (
                      <Skeleton key={i} className="h-12 rounded-sm" />
                    ))}
                  </div>
                )}
                {filteredReviews && filteredReviews.length === 0 && (
                  <p className="px-4 py-8 text-center text-[13px] text-muted-foreground">
                    {search ? `No reviews match “${search}”.` : "No reviews yet."}
                  </p>
                )}
                {filteredReviews && filteredReviews.length > 0 && (
                  <ul className="divide-y">
                    {filteredReviews.map((r) => {
                      const active = r.id === selectedId
                      return (
                        <li key={r.id}>
                          <button
                            type="button"
                            onClick={() => pickReview(r.id)}
                            className={cn(
                              "block w-full px-3 py-2.5 text-left transition-colors",
                              active ? "bg-secondary" : "hover:bg-secondary/50"
                            )}
                          >
                            <div className="flex items-center justify-between gap-3">
                              <span className="stamp truncate">
                                {r.repositoryFullName} · #{r.prNumber}
                              </span>
                              <StatusBadge status={r.status} />
                            </div>
                            <p className="mt-0.5 truncate text-[13px]">
                              {r.prTitle || "Untitled pull request"}
                            </p>
                          </button>
                        </li>
                      )
                    })}
                  </ul>
                )}
              </div>
            </div>
          )}

          <div className="flex items-center gap-1 rounded-md border bg-card pr-1 pl-1 focus-within:ring-1 focus-within:ring-ring">
            <Button
              type="button"
              variant="ghost"
              size="icon"
              onClick={() => setPickerOpen((v) => !v)}
              className={cn(
                "size-8 shrink-0 rounded-full text-muted-foreground hover:text-foreground",
                pickerOpen && "bg-secondary text-foreground"
              )}
              aria-label="Choose a review"
              aria-expanded={pickerOpen}
            >
              <Plus className="size-4" />
            </Button>
            <input
              value={question}
              onChange={(e) => setQuestion(e.target.value)}
              placeholder={
                selected ? "Ask about this pull request…" : "Select a review to ask a question"
              }
              disabled={!selected || streaming}
              className="h-10 flex-1 bg-transparent px-1 text-[13.5px] outline-none placeholder:text-muted-foreground/60 disabled:cursor-not-allowed"
            />
            <Button
              type="submit"
              size="icon"
              disabled={!selected || !question.trim() || streaming}
              className="size-8 shrink-0 rounded-full"
              aria-label="Send"
            >
              {streaming ? (
                <Loader2 className="size-3.5 animate-spin" />
              ) : (
                <ArrowUp className="size-3.5" />
              )}
            </Button>
          </div>
        </div>
      </form>
    </div>
  )
}

function parseSseBlock(block: string): { event: string; data: string } {
  let event = "message"
  const dataLines: string[] = []
  for (const line of block.split("\n")) {
    if (line.startsWith("event:")) event = line.slice(6).trim()
    else if (line.startsWith("data:")) dataLines.push(line.slice(5).replace(/^ /, ""))
  }
  return { event, data: dataLines.join("\n") }
}
