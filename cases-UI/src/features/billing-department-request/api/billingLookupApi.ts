import { apiClient } from '@/api/client'
import type { ApiSuccess } from '@/api/types'
import type { LookupItemDto } from '@/features/billing-department-request/types/billingTypes'

/**
 * Shared lookups that exist on LookupController today.
 * Campaigns / segments / products / parent cases are not exposed via REST yet —
 * callers should leave those dropdowns empty (do not invent options).
 */
export async function fetchActiveClients(): Promise<LookupItemDto[]> {
  try {
    const { data } = await apiClient.get<ApiSuccess<LookupItemDto[]>>('/api/v1/lookups/clients')
    return data.data ?? []
  } catch {
    return []
  }
}
