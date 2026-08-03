import { apiClient } from '../../../api/client'

/** Matches backend `LookupItem`. */
export type LookupItem = {
  id: string
  code: string
  label: string
}

/** Matches backend `EventIdLookupItem`. */
export type EventIdLookupItem = {
  code: string
  label: string
  mailMonth: string
}

export type DbmLookups = {
  clients: LookupItem[]
  eventIds: EventIdLookupItem[]
  spokenKeys: LookupItem[]
}

/** Loads all DBM create-screen lookups in one round-trip batch. */
export async function fetchDbmLookups(): Promise<DbmLookups> {
  const [clients, eventIds, spokenKeys] = await Promise.all([
    apiClient.get<LookupItem[]>('/api/lookups/clients').then((r) => r.data),
    apiClient
      .get<EventIdLookupItem[]>('/api/lookups/event-ids')
      .then((r) => r.data),
    apiClient.get<LookupItem[]>('/api/lookups/spoken-keys').then((r) => r.data),
  ])
  return { clients, eventIds, spokenKeys }
}
