import { useQuery } from '@tanstack/react-query'
import { fetchDbmLookups } from '../api/lookups'

export const DBM_LOOKUPS_QUERY_KEY = ['lookups', 'dbm-work-order'] as const

/** Loads clients, event IDs, and spoken keys when the create page mounts. */
export function useDbmLookups() {
  return useQuery({
    queryKey: DBM_LOOKUPS_QUERY_KEY,
    queryFn: fetchDbmLookups,
    staleTime: 5 * 60 * 1000,
    retry: 1,
  })
}
