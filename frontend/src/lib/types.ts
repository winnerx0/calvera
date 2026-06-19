export interface ApiResponse<T> {
  success: boolean
  data: T | null
  message: string | null
}

export type ReviewStatus = "PENDING" | "ANALYZING" | "DONE" | "FAILED"

export type Severity = "high" | "medium" | "low"

export interface BugFinding {
  file: string
  startLine: number
  endLine: number
  severity: Severity
  category: string
  title: string
  description: string
  suggestion: string | null
}

export interface PrReview {
  id: number
  deliveryId: string
  repositoryFullName: string
  prNumber: number
  prTitle: string | null
  action: string
  headSha: string | null
  baseSha: string | null
  status: ReviewStatus
  summary: string | null
  findings: BugFinding[]
  githubReviewId: number | null
  projectId: number
  createdAt: string
  updatedAt: string
}

export interface GithubRepo {
  id: number
  name: string
  fullName: string
  isPrivate: boolean
  description: string | null
  htmlUrl: string
}

export interface Project {
  id: number
  name: string
  repositoryName: string
  repositoryId: number
  createdAt: string
  updatedAt: string
}

export interface PullRequest {
  number: number
  title: string
  state: string
  draft: boolean
  headSha: string | null
  baseSha: string | null
  author: string | null
  htmlUrl: string
}
