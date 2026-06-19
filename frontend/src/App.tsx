import { BrowserRouter, Route, Routes } from "react-router-dom"
import AppShell from "@/components/AppShell"
import Login from "@/pages/Login"
import AuthCallback from "@/pages/AuthCallback"
import Chat from "@/pages/Chat"
import ReviewDetail from "@/pages/ReviewDetail"
import Projects from "@/pages/Projects"
import ProjectDetail from "@/pages/ProjectDetail"
import Settings from "@/pages/Settings"

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/auth/callback" element={<AuthCallback />} />
        <Route element={<AppShell />}>
          <Route path="/" element={<Chat />} />
          <Route path="/reviews/:id" element={<ReviewDetail />} />
          <Route path="/projects" element={<Projects />} />
          <Route path="/projects/:id" element={<ProjectDetail />} />
          <Route path="/settings" element={<Settings />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
