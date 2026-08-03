import { useMutation } from '@tanstack/react-query'
import axios from 'axios'
import { createDbmWorkOrder } from '../api/workOrders'
import type { CreateDbmWorkOrderRequest } from '../types/api'

function toUserFriendlyError(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const status = error.response?.status
    const data = error.response?.data as
      | { message?: string; error?: string | { message?: string } }
      | undefined

    if (typeof data?.message === 'string' && data.message.trim()) {
      return data.message
    }
    if (typeof data?.error === 'string' && data.error.trim()) {
      return data.error
    }
    if (
      data?.error &&
      typeof data.error === 'object' &&
      typeof data.error.message === 'string' &&
      data.error.message.trim()
    ) {
      return data.error.message
    }

    if (status === 400) {
      return 'Unable to create the case. Please check the form and try again.'
    }
    if (status === 401 || status === 403) {
      return 'You are not authorized to create this case.'
    }
    if (status && status >= 500) {
      return 'The server could not create the case. Please try again later.'
    }
    if (error.code === 'ERR_NETWORK') {
      return 'Unable to reach the server. Please try again.'
    }
  }

  if (error instanceof Error && error.message.trim()) {
    return error.message
  }

  return 'Failed to create the case. Please try again.'
}

/** Mutation hook for POST /api/dbm/work-orders. */
export function useCreateDbmWorkOrder() {
  return useMutation({
    mutationFn: (payload: CreateDbmWorkOrderRequest) =>
      createDbmWorkOrder(payload),
    meta: { toUserFriendlyError },
  })
}

export { toUserFriendlyError }
