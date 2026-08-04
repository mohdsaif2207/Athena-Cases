import { apiClient } from '@/api/client'
import type { ApiSuccess } from '@/api/types'

export async function fetchUserPreference(key: string): Promise<string | null> {
  try {
    const { data } = await apiClient.get<ApiSuccess<{ key: string; value: string }>>(
      `/api/v1/users/me/preferences/${encodeURIComponent(key)}`,
    )
    return data.data?.value ?? null
  } catch (error) {
    const status = (error as { response?: { status?: number } })?.response?.status
    if (status === 404) return null
    throw error
  }
}

export async function saveUserPreference(key: string, value: string): Promise<void> {
  await apiClient.put(`/api/v1/users/me/preferences/${encodeURIComponent(key)}`, { value })
}

export async function deleteUserPreference(key: string): Promise<void> {
  await apiClient.delete(`/api/v1/users/me/preferences/${encodeURIComponent(key)}`)
}
