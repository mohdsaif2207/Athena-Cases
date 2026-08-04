import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'
import type { ReactNode } from 'react'

interface RequirePermissionProps {
  anyOf: string[]
  children: ReactNode
  fallbackTo?: string
}

/**
 * Blocks a route unless the signed-in user has at least one of the given permissions.
 */
export function RequirePermission({ anyOf, children, fallbackTo = '/home' }: RequirePermissionProps) {
  const { user, isAuthenticated } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  const permissions = user?.permissions ?? []
  const allowed = anyOf.some((code) => permissions.includes(code))
  if (!allowed) {
    return <Navigate to={fallbackTo} replace />
  }

  return <>{children}</>
}
