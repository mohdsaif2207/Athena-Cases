/**
 * DBM access helpers — uses JWT receiving-team codes from AuthContext.
 * Receiving team {@code DBM_TEAM} matches seeded IAM (display name "DBM").
 */

import { useAuth } from '@/contexts/AuthContext'
import type { AuthenticatedUser } from '@/api/types'

/** Seeded receiving team code for DBM (must match backend {@code DbmConstants.RECEIVER_TEAM_DBM}). */
export const DBM_RECEIVING_TEAM_CODE = 'DBM_TEAM'

/** Whether the authenticated user is on the DBM receiving team (AC8). */
export function resolveIsDbmUser(user: AuthenticatedUser | null | undefined): boolean {
  const teams = user?.receivingTeams ?? []
  return teams.some(
    (code) => code.trim().toUpperCase() === DBM_RECEIVING_TEAM_CODE,
  )
}

/** Hook-shaped accessor so call sites stay stable. */
export function useIsDbmUser(): boolean {
  const { user } = useAuth()
  return resolveIsDbmUser(user)
}
