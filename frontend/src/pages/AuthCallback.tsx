import { useEffect } from "react"
import { useNavigate, useSearchParams } from "react-router-dom"
import { auth } from "@/lib/api"

export default function AuthCallback() {
  const [params] = useSearchParams()
  const navigate = useNavigate()

  useEffect(() => {
    const accessToken = params.get("accessToken")
    const refreshToken = params.get("refreshToken")
    if (accessToken && refreshToken) {
      auth.store(accessToken, refreshToken)
      navigate("/", { replace: true })
    } else {
      navigate("/login", { replace: true })
    }
  }, [params, navigate])

  return (
    <div className="flex min-h-screen items-center justify-center bg-background">
      <div className="flex items-center gap-2">
        <span className="h-1.5 w-1.5 animate-[pulse-dot_1.2s_ease-in-out_infinite] rounded-full bg-foreground" />
        <span className="micro-label">Signing in</span>
      </div>
    </div>
  )
}
