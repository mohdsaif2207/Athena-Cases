/**
 * TEMPORARY MOCK DATA — Billing feature only.
 * Replace when the corresponding backend lookup API is implemented
 * (GET /api/v1/lookups/campaigns|segments|products|parent-cases).
 *
 * Values mirror backend LookupMockData so referential validation can succeed.
 */
import type { LookupItemDto } from '@/features/billing-department-request/types/billingTypes'

export const BILLING_TEMP_CAMPAIGNS: LookupItemDto[] = [
  { id: 'CMP001', code: 'CMP001', label: 'Q3 Retention' },
  { id: 'CMP002', code: 'CMP002', label: 'New Member Drive' },
  { id: 'CMP003', code: 'CMP003', label: 'Billing Hold Pilot' },
]

export const BILLING_TEMP_PRODUCTS: LookupItemDto[] = [
  { id: '101', code: 'PRD001', label: 'Checking' },
  { id: '102', code: 'PRD002', label: 'Savings' },
  { id: '103', code: 'PRD003', label: 'Credit Card' },
  { id: '104', code: 'PCP001', label: 'Northside Family PCP' },
  { id: '105', code: 'PCP002', label: 'Riverside Primary Care' },
]

export const BILLING_TEMP_PARENT_CASES: LookupItemDto[] = [
  { id: '9001', code: 'BIL9001', label: 'BIL9001 — Sample Parent Billing Case' },
  { id: '9002', code: 'BIL9002', label: 'BIL9002 — Sample Parent Research Case' },
  { id: '9003', code: 'DBM9003', label: 'DBM9003 — Sample Parent DBM Case' },
]

/** Keyed by client id/code (matches LookupMockData.CLIENTS). */
export const BILLING_TEMP_SEGMENTS_BY_CLIENT: Record<string, LookupItemDto[]> = {
  CLIENT001: [
    { id: '201', code: 'SEG201', label: 'ABC Retail Segment' },
    { id: '202', code: 'SEG202', label: 'ABC Commercial Segment' },
  ],
  CLIENT002: [
    { id: '203', code: 'SEG203', label: 'XYZ Standard Segment' },
    { id: '204', code: 'SEG204', label: 'XYZ Premium Segment' },
  ],
  CLIENT003: [{ id: '205', code: 'SEG205', label: 'FNCU Member Segment' }],
}

export function billingTempSegmentsForClient(clientId: string): LookupItemDto[] {
  if (!clientId.trim()) return []
  return BILLING_TEMP_SEGMENTS_BY_CLIENT[clientId.trim()] ?? []
}
