/**
 * Temporary frontend access helpers until real auth/RBAC is wired.
 *
 * Replace {@link resolveIsDbmUser} with the authentication/authorization service
 * (JWT claims, role API, etc.) — do not push auth rules into the backend from here.
 */

/** Flip this stub while developing; later: read role from the auth service. */
const TEMP_IS_DBM_USER = true

/**
 * Whether the current user may see DBM-only fields ("For DBM Use Only").
 * Easy swap point for the real authorization service.
 */
export function resolveIsDbmUser(): boolean {
  return TEMP_IS_DBM_USER
}

/** Hook-shaped accessor so call sites stay stable when auth becomes async. */
export function useIsDbmUser(): boolean {
  return resolveIsDbmUser()
}
