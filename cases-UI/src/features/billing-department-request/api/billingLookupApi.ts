import { apiClient } from '@/api/client'
import type { ApiSuccess } from '@/api/types'
import type { LookupItemDto } from '@/features/billing-department-request/types/billingTypes'
import {
  BILLING_TEMP_CAMPAIGNS,
  BILLING_TEMP_PRODUCTS,
  billingTempSegmentsForClient,
} from '@/features/billing-department-request/constants/billingTemporaryLookups'

const BILLING_LOOKUPS = '/api/v1/billing/lookups'

/**
 * Clients — shared lookup (not Billing-owned).
 */
export async function fetchActiveClients(): Promise<LookupItemDto[]> {
  try {
    const { data } = await apiClient.get<ApiSuccess<LookupItemDto[]>>('/api/v1/lookups/clients')
    return data.data ?? []
  } catch {
    return []
  }
}

/**
 * Campaigns — Billing lookup module.
 */
export async function fetchCampaigns(): Promise<LookupItemDto[]> {
  try {
    const { data } = await apiClient.get<ApiSuccess<LookupItemDto[]>>(`${BILLING_LOOKUPS}/campaigns`)
    if (Array.isArray(data.data) && data.data.length) return data.data
  } catch {
    /* use temporary Billing mock */
  }
  return BILLING_TEMP_CAMPAIGNS
}

/**
 * Products / PCP — Billing lookup module.
 */
export async function fetchProducts(): Promise<LookupItemDto[]> {
  try {
    const { data } = await apiClient.get<ApiSuccess<LookupItemDto[]>>(`${BILLING_LOOKUPS}/products`)
    if (Array.isArray(data.data) && data.data.length) return data.data
  } catch {
    /* use temporary Billing mock */
  }
  return BILLING_TEMP_PRODUCTS
}

/**
 * Parent cases — Billing lookup module (real cases.id).
 */
export async function fetchParentCases(): Promise<LookupItemDto[]> {
  try {
    const { data } = await apiClient.get<ApiSuccess<LookupItemDto[]>>(`${BILLING_LOOKUPS}/parent-cases`)
    if (Array.isArray(data.data)) return data.data
  } catch {
    /* do not offer fake parent ids that fail Save */
  }
  return []
}

/**
 * Segments for client — Billing lookup module.
 */
export async function fetchSegmentsForClient(clientId: string): Promise<LookupItemDto[]> {
  if (!clientId.trim()) return []
  try {
    const { data } = await apiClient.get<ApiSuccess<LookupItemDto[]>>(`${BILLING_LOOKUPS}/segments`, {
      params: { clientId },
    })
    if (Array.isArray(data.data)) return data.data
  } catch {
    /* use temporary Billing mock */
  }
  return billingTempSegmentsForClient(clientId)
}
