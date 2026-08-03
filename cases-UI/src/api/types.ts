export interface AuthenticatedUser {
  id: number
  username: string
  displayName: string
  roles: string[]
  permissions: string[]
  groups?: string[]
  teams?: string[]
  caseTypes?: string[]
  initiatingTeams?: string[]
  receivingTeams?: string[]
}

export interface LoginResponseData {
  accessToken: string
  tokenType: string
  expiresInMinutes: number
  user: AuthenticatedUser
}

export interface ApiSuccess<T> {
  data: T
  request_id?: string
  requestId?: string
  timestamp: string
}

export interface ApiErrorBody {
  error: {
    code: string
    message: string
    field: string | null
    request_id?: string
    requestId?: string
    timestamp: string
    details: Array<{ field: string; message: string; code: string }>
  }
}
