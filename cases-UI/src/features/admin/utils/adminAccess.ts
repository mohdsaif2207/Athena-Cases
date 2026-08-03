import type { AuthenticatedUser } from '@/api/types'

/** True when the user may open User Management (ADMIN_ACCESS or SYSTEM_ADMINISTRATOR). */
export function canAccessAdmin(user: AuthenticatedUser | null | undefined): boolean {
  if (!user) return false
  return (
    user.permissions?.includes('ADMIN_ACCESS') === true ||
    user.roles?.includes('SYSTEM_ADMINISTRATOR') === true
  )
}
