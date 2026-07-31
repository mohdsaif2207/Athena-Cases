import axios from 'axios'
import type { ApiEnvelope, ExrtCaseCreatePayload, ExrtCaseCreateResult, LookupItem } from '../types/exrt.types'
import { EXRT_API_BASE, EXRT_LOOKUP_BASE } from '../theme/exrtTheme'

/**
 * Vite only exposes VITE_* vars. Prefer VITE_API_BASE_URL (canonical);
 * accept VITE_API_URL as an alias used in some local .env files.
 * Default matches cases-MT local port (8081), not docker-compose internal 8090.
 */
function resolveApiBaseUrl(): string {
  const fromEnv =
    import.meta.env.VITE_API_BASE_URL ||
    import.meta.env.VITE_API_URL ||
    ''
  const trimmed = String(fromEnv).trim()
  if (trimmed) {
    return trimmed.replace(/\/$/, '')
  }
  return 'http://localhost:8081'
}

const api = axios.create({
  baseURL: resolveApiBaseUrl(),
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  // Auth header only — does not rewrite URL/path
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
