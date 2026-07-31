import { apiClient } from '@/api/client'
import type { ApiSuccess, AuthenticatedUser, LoginResponseData } from '@/api/types'

export async function loginRequest(username: string, password: string): Promise<LoginResponseData> {
  const { data } = await apiClient.post<ApiSuccess<LoginResponseData>>('/api/v1/auth/login', {
    username,
    password,
  })
  return data.data
}

/** Loads the authenticated profile from the users table (includes displayName). */
export async function fetchCurrentUser(): Promise<AuthenticatedUser> {
  const { data } = await apiClient.get<ApiSuccess<AuthenticatedUser>>('/api/v1/auth/me')
  return data.data
}
