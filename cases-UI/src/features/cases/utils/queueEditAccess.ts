import type { AuthenticatedUser } from '@/api/types'

/**
 * Edit on Home queues: user must be on the row's receiving team
 * (Billing Ops / DBM / ExRT — including admin with those team scopes).
 */
export function canEditQueueRecord(
  user: AuthenticatedUser | null | undefined,
  _createdBy: string | undefined,
  receivingTeamCode: string | undefined,
): boolean {
  if (!user) return false

  const team = (receivingTeamCode ?? '').trim()
  if (!team) return false

  const userTeams = user.receivingTeams ?? []
  return userTeams.some((t) => t.toLowerCase() === team.toLowerCase())
}
