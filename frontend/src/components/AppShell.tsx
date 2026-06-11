import { Navigate, NavLink, Outlet, useNavigate } from "react-router-dom"
import { LogOut } from "lucide-react"
import { auth } from "@/lib/api"
import { Button } from "@/components/ui/button"
import { cn } from "@/lib/utils"

const links = [
  { to: "/", label: "Events" },
  { to: "/projects", label: "Projects" },
  { to: "/settings", label: "Settings" },
]

export default function AppShell() {
  const navigate = useNavigate()

  if (!auth.isAuthenticated()) {
    return <Navigate to="/login" replace />
  }

  return (
    <div className="flex min-h-screen flex-col bg-background">
      <header className="sticky top-0 z-20 border-b bg-background/90 backdrop-blur-sm">
        <div className="mx-auto flex h-14 w-full max-w-5xl items-center justify-between px-6">
          <div className="flex items-center gap-10">
            <NavLink to="/" className="flex items-center gap-2">
              <span className="h-1.5 w-1.5 rounded-full bg-signal" />
              <span className="text-[13px] font-medium tracking-[0.18em] uppercase">
                Calvera
              </span>
            </NavLink>
            <nav className="flex items-center gap-1">
              {links.map((link) => (
                <NavLink
                  key={link.to}
                  to={link.to}
                  end={link.to === "/"}
                  className={({ isActive }) =>
                    cn(
                      "rounded-md px-3 py-1.5 text-[13px] transition-colors",
                      isActive
                        ? "bg-secondary font-medium text-foreground"
                        : "text-muted-foreground hover:text-foreground"
                    )
                  }
                >
                  {link.label}
                </NavLink>
              ))}
            </nav>
          </div>
          <Button
            variant="ghost"
            size="sm"
            className="gap-2 text-[13px] text-muted-foreground hover:text-foreground"
            onClick={() => {
              auth.clear()
              navigate("/login")
            }}
          >
            <LogOut className="size-3.5" />
            Sign out
          </Button>
        </div>
      </header>

      <main className="mx-auto w-full max-w-5xl flex-1 px-6 py-10">
        <Outlet />
      </main>

      <footer className="border-t">
        <div className="mx-auto flex w-full max-w-5xl items-center justify-between px-6 py-4">
          <span className="font-mono text-[11px] text-muted-foreground/50">calvera</span>
          <span className="font-mono text-[11px] text-muted-foreground/50">
            listening for workflow_run
          </span>
        </div>
      </footer>
    </div>
  )
}
