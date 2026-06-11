import type { ApiResponse } from "./types"

const ACCESS_TOKEN_KEY = "calvera.accessToken"
const REFRESH_TOKEN_KEY = "calvera.refreshToken"

export const auth = {
  get accessToken() {
    return localStorage.getItem(ACCESS_TOKEN_KEY)
  },
  isAuthenticated() {
    return Boolean(localStorage.getItem(ACCESS_TOKEN_KEY))
  },
  store(accessToken: string, refreshToken: string) {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  },
  clear() {
    localStorage.removeItem(ACCESS_TOKEN_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
  },
}

export class ApiError extends Error {
  status: number
  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const headers = new Headers(init?.headers)
  headers.set("Content-Type", "application/json")
  const token = auth.accessToken
  if (token) headers.set("Authorization", `Bearer ${token}`)

  const response = await fetch(path, { ...init, headers })

  if (response.status === 401) {
    auth.clear()
    window.location.assign("/login")
    throw new ApiError(401, "Session expired")
  }

  if (response.status === 204) return undefined as T

  const body = (await response.json().catch(() => null)) as ApiResponse<T> | null

  if (!response.ok || !body?.success) {
    throw new ApiError(response.status, body?.message ?? `Request failed (${response.status})`)
  }

  return body.data as T
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, data: unknown) =>
    request<T>(path, { method: "POST", body: JSON.stringify(data) }),
  patch: <T>(path: string, data: unknown) =>
    request<T>(path, { method: "PATCH", body: JSON.stringify(data) }),
  delete: <T>(path: string) => request<T>(path, { method: "DELETE" }),
}
