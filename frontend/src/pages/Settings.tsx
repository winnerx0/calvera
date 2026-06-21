import { useEffect, useState } from "react"
import { api } from "@/lib/api"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Skeleton } from "@/components/ui/skeleton"

interface UserView {
  id: number
  username: string
  email: string
  picture: string | null
  joinedAt: string
}

export default function Settings() {
  const [user, setUser] = useState<UserView | null>(null)
  const [username, setUsername] = useState("")
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)
  const [saved, setSaved] = useState(false)

  useEffect(() => {
    api
      .get<UserView>("/api/user/me")
      .then((u) => {
        setUser(u)
        setUsername(u.username)
      })
      .catch((e: Error) => setError(e.message))
  }, [])

  const save = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!user || username.trim() === user.username) return
    setSaving(true)
    setError(null)
    try {
      const updated = await api.put<UserView>("/api/user/me", { username: username.trim() })
      setUser(updated)
      setUsername(updated.username)
      setSaved(true)
      setTimeout(() => setSaved(false), 2000)
    } catch (err) {
      setError((err as Error).message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="rise">
      <div className="mb-8">
        <p className="micro-label mb-2">Account</p>
        <h1 className="text-2xl font-light tracking-tight">Settings</h1>
      </div>

      {error && (
        <div className="mb-6 rounded-md border border-signal/25 bg-signal/5 px-4 py-3 text-[13px] text-signal">
          {error}
        </div>
      )}

      {!user && !error && <Skeleton className="h-48 rounded-md" />}

      {user && (
        <section className="rounded-md border bg-card p-6">
          <div className="mb-6 flex items-center gap-4">
            {user.picture && (
              <img src={user.picture} alt={user.username} className="size-12 rounded-full border" />
            )}
            <div>
              <p className="text-[13.5px] font-medium">{user.email}</p>
              <p className="mt-0.5 text-[12px] text-muted-foreground">
                Joined {new Date(user.joinedAt).toLocaleDateString()}
              </p>
            </div>
          </div>

          <form onSubmit={save} className="space-y-4">
            <div>
              <label className="micro-label mb-1.5 block">Username</label>
              <Input
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className="text-[13px]"
              />
            </div>
            <div className="flex items-center gap-3">
              <Button
                type="submit"
                size="sm"
                className="text-[13px]"
                disabled={saving || username.trim() === "" || username.trim() === user.username}
              >
                {saving ? "Saving…" : "Save"}
              </Button>
              {saved && <span className="text-[12px] text-jade">Saved</span>}
            </div>
          </form>
        </section>
      )}
    </div>
  )
}
