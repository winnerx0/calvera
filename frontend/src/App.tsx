import { BrowserRouter, Route, Routes } from "react-router-dom"
import AppShell from "@/components/AppShell"
import Login from "@/pages/Login"
import AuthCallback from "@/pages/AuthCallback"
import Events from "@/pages/Events"
import EventDetail from "@/pages/EventDetail"
import Projects from "@/pages/Projects"
import Settings from "@/pages/Settings"

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/auth/callback" element={<AuthCallback />} />
        <Route element={<AppShell />}>
          <Route path="/" element={<Events />} />
          <Route path="/events/:id" element={<EventDetail />} />
          <Route path="/projects" element={<Projects />} />
          <Route path="/settings" element={<Settings />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
