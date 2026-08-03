import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'
import { canAccessAdmin } from '@/features/admin/utils/adminAccess'
import type { ReactNode } from 'react'

/**
 * Requires authentication plus ADMIN_ACCESS or SYSTEM_ADMINISTRATOR.
 */
export function AdminRoute({ children }: { children: ReactNode }) {
  const { isAuthenticated, user } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  if (!canAccessAdmin(user)) {
    return <Navigate to="/cases" replace />
  }

  return <>{children}</>
}
