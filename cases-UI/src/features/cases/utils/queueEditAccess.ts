import type { AuthenticatedUser } from '@/api/types'
import { canAccessAdmin } from '@/features/admin/utils/adminAccess'

/** Edit on Home queues: receiving-team member AND (admin OR record creator). */
export function canEditQueueRecord(
  user: AuthenticatedUser | null | undefined,
  createdBy: string | undefined,
  receivingTeamCode: string | undefined,
): boolean {
  if (!user) return false

  const team = (receivingTeamCode ?? '').trim()
  if (!team) return false

  const userTeams = user.receivingTeams ?? []
  const onReceivingTeam = userTeams.some((t) => t.toLowerCase() === team.toLowerCase())
  if (!onReceivingTeam) return false

  if (canAccessAdmin(user)) return true

  const creator = (createdBy ?? '').trim().toLowerCase()
  if (!creator) return false
  return (
    creator === user.username.toLowerCase() ||
    creator === (user.displayName ?? '').toLowerCase()
  )
}
