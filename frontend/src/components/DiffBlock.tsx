import { useMemo } from "react"

type LineKind = "add" | "del" | "ctx"
type DiffLine = { kind: LineKind; text: string; oldNo?: number; newNo?: number }
type Hunk = { header: string; lines: DiffLine[] }
type ParsedDiff = { file: string; hunks: Hunk[] }

function parseDiff(raw: string): ParsedDiff {
  const lines = raw.split("\n")
  let file = ""
  const hunks: Hunk[] = []
  let current: Hunk | null = null
  let oldNo = 0
  let newNo = 0

  for (const line of lines) {
    if (line.startsWith("+++ ")) {
      file = line.slice(4).replace(/^b\//, "").trim()
      continue
    }
    if (line.startsWith("--- ")) continue
    if (line.startsWith("@@")) {
      const match = /@@ -(\d+)(?:,\d+)? \+(\d+)(?:,\d+)? @@/.exec(line)
      if (match) {
        oldNo = parseInt(match[1], 10)
        newNo = parseInt(match[2], 10)
      }
      current = { header: line, lines: [] }
      hunks.push(current)
      continue
    }
    if (!current) continue
    if (line.startsWith("+")) {
      current.lines.push({ kind: "add", text: line.slice(1), newNo: newNo++ })
    } else if (line.startsWith("-")) {
      current.lines.push({ kind: "del", text: line.slice(1), oldNo: oldNo++ })
    } else {
      const text = line.startsWith(" ") ? line.slice(1) : line
      current.lines.push({ kind: "ctx", text, oldNo: oldNo++, newNo: newNo++ })
    }
  }
  return { file, hunks }
}

type Row = { left: DiffLine | null; right: DiffLine | null }

function pairForSplit(hunk: Hunk): Row[] {
  const rows: Row[] = []
  const lines = hunk.lines
  let i = 0
  while (i < lines.length) {
    const l = lines[i]
    if (l.kind === "ctx") {
      rows.push({ left: l, right: l })
      i++
      continue
    }
    if (l.kind === "del") {
      const dels: DiffLine[] = []
      while (i < lines.length && lines[i].kind === "del") {
        dels.push(lines[i])
        i++
      }
      const adds: DiffLine[] = []
      while (i < lines.length && lines[i].kind === "add") {
        adds.push(lines[i])
        i++
      }
      const max = Math.max(dels.length, adds.length)
      for (let k = 0; k < max; k++) {
        rows.push({ left: dels[k] ?? null, right: adds[k] ?? null })
      }
      continue
    }
    rows.push({ left: null, right: l })
    i++
  }
  return rows
}

function DiffCell({ line, side }: { line: DiffLine | null; side: "left" | "right" }) {
  if (!line) {
    return <div className="bg-muted/40 py-0.5 leading-6">&nbsp;</div>
  }
  const bg =
    line.kind === "add"
      ? "bg-green-50"
      : line.kind === "del"
      ? "bg-red-50"
      : ""
  const fg =
    line.kind === "add"
      ? "text-green-900"
      : line.kind === "del"
      ? "text-red-900"
      : "text-foreground"
  const marker = line.kind === "add" ? "+" : line.kind === "del" ? "-" : " "
  const lineNo = side === "left" ? line.oldNo : line.newNo
  return (
    <div className={`grid grid-cols-[44px_18px_1fr] py-0.5 leading-6 ${bg} ${fg}`}>
      <span className="select-none border-r border-border/50 px-2 text-right text-[11px] text-muted-foreground tabular-nums">
        {lineNo ?? ""}
      </span>
      <span className="select-none px-1 text-muted-foreground">{marker}</span>
      <span className="whitespace-pre overflow-x-auto px-2">{line.text}</span>
    </div>
  )
}

export function DiffBlock({ raw }: { raw: string }) {
  const parsed = useMemo(() => parseDiff(raw), [raw])
  const { adds, dels } = useMemo(() => {
    let adds = 0
    let dels = 0
    for (const h of parsed.hunks) {
      for (const l of h.lines) {
        if (l.kind === "add") adds++
        else if (l.kind === "del") dels++
      }
    }
    return { adds, dels }
  }, [parsed])

  return (
    <div className="my-5 overflow-hidden rounded-md border bg-card font-mono text-[13px]">
      {/* Patch envelope — names the file and summarises the change like a git command. */}
      <div className="flex items-center justify-between gap-4 border-b bg-secondary/60 px-4 py-2.5">
        <div className="flex min-w-0 items-center gap-2 text-[12px] text-muted-foreground">
          <span className="select-none text-foreground/55">▸</span>
          <span className="select-none uppercase tracking-wider text-foreground/55">patch</span>
          <span className="select-none text-muted-foreground/40">·</span>
          <span className="truncate text-foreground/85">{parsed.file || "unknown file"}</span>
        </div>
        <div className="flex shrink-0 items-center gap-3 text-[11.5px] tabular-nums">
          <span className="text-green-700">+{adds}</span>
          <span className="text-red-700">−{dels}</span>
        </div>
      </div>
      {parsed.hunks.map((hunk, hi) => {
        const rows = pairForSplit(hunk)
        return (
          <div key={hi}>
            <div className="border-b border-border/60 bg-secondary/30 px-4 py-1.5 text-[11.5px] text-muted-foreground">
              {hunk.header}
            </div>
            <div className="grid grid-cols-2 divide-x">
              <div>
                {rows.map((row, ri) => (
                  <DiffCell key={ri} line={row.left} side="left" />
                ))}
              </div>
              <div>
                {rows.map((row, ri) => (
                  <DiffCell key={ri} line={row.right} side="right" />
                ))}
              </div>
            </div>
          </div>
        )
      })}
    </div>
  )
}
