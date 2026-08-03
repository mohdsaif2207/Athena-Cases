import { isAxiosError } from 'axios'
import type { ApiErrorBody } from '@/api/types'

export function getErrorMessage(error: unknown, fallback = 'Something went wrong. Please try again.'): string {
  if (isAxiosError(error)) {
    const data = error.response?.data as ApiErrorBody | undefined
    if (data?.error?.message) {
      return data.error.message
    }
    if (error.response?.status === 401) {
      return 'Invalid username or password.'
    }
    if (!error.response) {
      return 'Unable to reach the server. Please check your connection and try again.'
    }
  }
  if (error instanceof Error && error.message) {
    return error.message
  }
  return fallback
}
