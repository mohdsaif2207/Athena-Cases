import axios from 'axios'
import type { ApiEnvelope, ExrtCaseCreatePayload, ExrtCaseCreateResult, LookupItem } from '../types/exrt.types'
import { EXRT_API_BASE, EXRT_LOOKUP_BASE } from '../theme/exrtTheme'

/** Single source of truth: VITE_API_BASE_URL (see cases-UI/.env). Local default: :8090. */
export function resolveApiBaseUrl(): string {
  const fromEnv = String(import.meta.env.VITE_API_BASE_URL ?? '').trim()
  if (fromEnv) {
    return fromEnv.replace(/\/$/, '')
  }
  return 'http://localhost:8090'
}

const api = axios.create({
  baseURL: resolveApiBaseUrl(),
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  // Auth header only — does not rewrite baseURL or path
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

async function unwrap<T>(promise: Promise<{ data: ApiEnvelope<T> }>): Promise<T> {
  const { data } = await promise
  return data.data
}

export function createExrtCase(payload: ExrtCaseCreatePayload) {
  return unwrap(api.post<ApiEnvelope<ExrtCaseCreateResult>>(EXRT_API_BASE, payload))
}

export function fetchExrtEnumLookup(lookupType: string) {
  return unwrap(api.get<ApiEnvelope<LookupItem[]>>(`${EXRT_LOOKUP_BASE}/enums/${lookupType}`))
}

export function fetchCarriers() {
  return unwrap(api.get<ApiEnvelope<LookupItem[]>>(`${EXRT_LOOKUP_BASE}/carriers`))
}

export function fetchTierIiAgents() {
  return unwrap(api.get<ApiEnvelope<LookupItem[]>>(`${EXRT_LOOKUP_BASE}/tier-ii-agents`))
}

export function fetchAssignees() {
  return unwrap(api.get<ApiEnvelope<LookupItem[]>>(`${EXRT_LOOKUP_BASE}/assignees`))
}

export function fetchContacts(clientId?: string) {
  return unwrap(
    api.get<ApiEnvelope<LookupItem[]>>(`${EXRT_LOOKUP_BASE}/contacts`, {
      params: clientId ? { clientId } : undefined,
    }),
  )
}

export function fetchReasonCodes() {
  return unwrap(api.get<ApiEnvelope<LookupItem[]>>(`${EXRT_LOOKUP_BASE}/reason-codes`))
}
