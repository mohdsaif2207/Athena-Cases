import { apiClient } from '@/api/client'
import type { ApiSuccess } from '@/api/types'
import type {
  BillingAssigneeDto,
  BillingDepartmentRequestCreatePayload,
  BillingDepartmentRequestResponseDto,
  BillingDepartmentRequestUpdatePayload,
  HoldLevelsLookupDto,
} from '@/features/billing-department-request/types/billingTypes'

const BASE = '/api/v1/billing-department-requests'

export async function createBillingDepartmentRequest(
  payload: BillingDepartmentRequestCreatePayload,
): Promise<BillingDepartmentRequestResponseDto> {
  const { data } = await apiClient.post<ApiSuccess<BillingDepartmentRequestResponseDto>>(BASE, payload)
  return data.data
}

export async function getBillingDepartmentRequest(
  caseId: number,
): Promise<BillingDepartmentRequestResponseDto> {
  const { data } = await apiClient.get<ApiSuccess<BillingDepartmentRequestResponseDto>>(
    `${BASE}/${caseId}`,
  )
  return data.data
}

export async function updateBillingDepartmentRequest(
  caseId: number,
  payload: BillingDepartmentRequestUpdatePayload,
): Promise<BillingDepartmentRequestResponseDto> {
  const { data } = await apiClient.put<ApiSuccess<BillingDepartmentRequestResponseDto>>(
    `${BASE}/${caseId}`,
    payload,
  )
  return data.data
}

export async function fetchBillingHoldLevels(): Promise<string[]> {
  const { data } = await apiClient.get<ApiSuccess<HoldLevelsLookupDto>>(`${BASE}/lookups/hold-levels`)
  return data.data?.available ?? []
}

export async function fetchBillingAssignees(): Promise<BillingAssigneeDto[]> {
  const { data } = await apiClient.get<ApiSuccess<BillingAssigneeDto[]>>(`${BASE}/lookups/assignees`)
  return data.data ?? []
}
