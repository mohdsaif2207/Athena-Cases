import { apiClient } from '@/api/client'
import type { ApiSuccess } from '@/api/types'

export interface CaseTypeOption {
  id: string
  code: string
  label: string
}

interface LookupItemDto {
  id: string
  code: string
  label: string
}

/**
 * Loads case types the authenticated user may create (RBAC-filtered from case_types).
 */
export async function fetchAuthorizedCaseTypes(): Promise<CaseTypeOption[]> {
  const { data } = await apiClient.get<ApiSuccess<LookupItemDto[]>>('/api/v1/lookups/case-types')
  return (data.data ?? []).map((item) => ({
    id: item.id,
    code: item.code,
    label: item.label,
  }))
}
