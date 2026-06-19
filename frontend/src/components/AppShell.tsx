import { Navigate, NavLink, Outlet, useLocation, useNavigate } from "react-router-dom"
import { FolderGit2, LogOut, MessageSquare, Settings as SettingsIcon } from "lucide-react"
import { auth } from "@/lib/api"
import { cn } from "@/lib/utils"
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarHeader,
  SidebarInset,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarProvider,
  SidebarRail,
  SidebarTrigger,
  useSidebar,
} from "@/components/ui/sidebar"

const links = [
  { to: "/", label: "Chat", icon: MessageSquare, end: true },
  { to: "/projects", label: "Projects", icon: FolderGit2, end: false },
  { to: "/settings", label: "Settings", icon: SettingsIcon, end: false },
] as const

function BrandMark() {
  const { state } = useSidebar()
  const collapsed = state === "collapsed"
  return (
    <NavLink
      to="/"
      className={cn(
        "flex items-center gap-2.5 px-2 py-1",
        collapsed && "justify-center px-0"
      )}
      aria-label="Calvera home"
    >
      <span className="relative flex h-2 w-2 items-center justify-center">
        <span className="absolute h-2 w-2 rounded-full bg-signal/30 animate-[pulse-dot_1.6s_ease-in-out_infinite]" />
        <span className="h-1 w-1 rounded-full bg-signal" />
      </span>
      {!collapsed && (
        <span className="text-[12.5px] font-medium tracking-[0.22em] uppercase">
          Calvera
        </span>
      )}
    </NavLink>
  )
}

function SignOutButton() {
  const navigate = useNavigate()
  return (
    <SidebarMenuButton
      tooltip="Sign out"
      onClick={() => {
        auth.clear()
        navigate("/login")
      }}
      className="text-muted-foreground hover:text-foreground"
    >
      <LogOut />
      <span>Sign out</span>
    </SidebarMenuButton>
  )
}

function NavSection() {
  const { pathname } = useLocation()
  return (
    <SidebarMenu>
      {links.map(({ to, label, icon: Icon, end }) => {
        const isActive = end ? pathname === to : pathname.startsWith(to)
        return (
          <SidebarMenuItem key={to}>
            <SidebarMenuButton asChild isActive={isActive} tooltip={label}>
              <NavLink to={to} end={end}>
                <Icon />
                <span>{label}</span>
              </NavLink>
            </SidebarMenuButton>
          </SidebarMenuItem>
        )
      })}
    </SidebarMenu>
  )
}

export default function AppShell() {
  if (!auth.isAuthenticated()) {
    return <Navigate to="/login" replace />
  }

  return (
    <SidebarProvider>
      <Sidebar collapsible="icon" className="border-r">
        <SidebarHeader className="border-b py-3">
          <BrandMark />
        </SidebarHeader>
        <SidebarContent>
          <SidebarGroup>
            <NavSection />
          </SidebarGroup>
        </SidebarContent>
        <SidebarFooter className="border-t py-2">
          <SidebarMenu>
            <SidebarMenuItem>
              <SignOutButton />
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarFooter>
        <SidebarRail />
      </Sidebar>

      <SidebarInset className="bg-background">
        <header className="sticky top-0 z-10 flex h-12 items-center gap-3 border-b bg-background/85 px-4 backdrop-blur-sm">
          <SidebarTrigger className="text-muted-foreground hover:text-foreground" />
          <span className="font-mono text-[11px] tracking-wider text-muted-foreground/70 uppercase">
            listening for pull_request
          </span>
        </header>
        <main className="mx-auto w-full max-w-5xl flex-1 px-6 py-10">
          <Outlet />
        </main>
        <footer className="border-t">
          <div className="mx-auto flex w-full max-w-5xl items-center justify-between px-6 py-4">
            <span className="font-mono text-[11px] text-muted-foreground/50">calvera</span>
            <span className="font-mono text-[11px] text-muted-foreground/50">
              {new Date().getFullYear()}
            </span>
          </div>
        </footer>
      </SidebarInset>
    </SidebarProvider>
  )
}
