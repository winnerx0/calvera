export interface ApiResponse<T> {
  success: boolean
  data: T | null
  message: string | null
}

export type CiEventStatus = "PENDING" | "ANALYZING" | "DONE" | "FAILED"

export interface CiEvent {
  id: number
  deliveryId: string
  repositoryFullName: string
  workflowName: string
  conclusion: string
  logsUrl: string
  status: CiEventStatus
  analysisResult: string | null
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
