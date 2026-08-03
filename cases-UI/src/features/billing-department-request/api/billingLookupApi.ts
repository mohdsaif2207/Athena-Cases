import { apiClient } from '@/api/client'
import type { ApiSuccess } from '@/api/types'
import type { LookupItemDto } from '@/features/billing-department-request/types/billingTypes'
import {
  BILLING_TEMP_CAMPAIGNS,
  BILLING_TEMP_PARENT_CASES,
  BILLING_TEMP_PRODUCTS,
  billingTempSegmentsForClient,
} from '@/features/billing-department-request/constants/billingTemporaryLookups'

/**
 * Clients — real shared lookup endpoint.
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
 * Campaigns — try REST; fall back to Billing temporary mock (no LookupController route yet).
 */
export async function fetchCampaigns(): Promise<LookupItemDto[]> {
  try {
    const { data } = await apiClient.get<ApiSuccess<LookupItemDto[]>>('/api/v1/lookups/campaigns')
    if (Array.isArray(data.data) && data.data.length) return data.data
  } catch {
    /* endpoint missing — use temporary Billing mock */
  }
  return BILLING_TEMP_CAMPAIGNS
}

/**
 * Products / PCP — try REST; fall back to Billing temporary mock.
 */
export async function fetchProducts(): Promise<LookupItemDto[]> {
  try {
    const { data } = await apiClient.get<ApiSuccess<LookupItemDto[]>>('/api/v1/lookups/products')
    if (Array.isArray(data.data) && data.data.length) return data.data
  } catch {
    /* endpoint missing — use temporary Billing mock */
  }
  return BILLING_TEMP_PRODUCTS
}

/**
 * Parent cases — try REST; fall back to Billing temporary mock.
 */
export async function fetchParentCases(): Promise<LookupItemDto[]> {
  try {
    const { data } = await apiClient.get<ApiSuccess<LookupItemDto[]>>('/api/v1/lookups/parent-cases')
    if (Array.isArray(data.data) && data.data.length) return data.data
  } catch {
    /* endpoint missing — use temporary Billing mock */
  }
  return BILLING_TEMP_PARENT_CASES
}

/**
 * Segments for client — try REST; fall back to Billing temporary mock.
 */
export async function fetchSegmentsForClient(clientId: string): Promise<LookupItemDto[]> {
  if (!clientId.trim()) return []
  try {
    const { data } = await apiClient.get<ApiSuccess<LookupItemDto[]>>('/api/v1/lookups/segments', {
      params: { clientId },
    })
    if (Array.isArray(data.data)) return data.data
  } catch {
    /* endpoint missing — use temporary Billing mock */
  }
  return billingTempSegmentsForClient(clientId)
}
