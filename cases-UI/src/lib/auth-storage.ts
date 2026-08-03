import type { AuthenticatedUser } from '@/api/types'

const TOKEN_KEY = 'athena.cases.accessToken'
const USER_KEY = 'athena.cases.user'

/** sessionStorage — cleared when the browser tab closes (safer than localStorage for JWTs). */
export function getAccessToken(): string | null {
  return sessionStorage.getItem(TOKEN_KEY)
}

export function getStoredUser(): AuthenticatedUser | null {
  const raw = sessionStorage.getItem(USER_KEY)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as AuthenticatedUser
  } catch {
    return null
  }
}

export function persistSession(token: string, user: AuthenticatedUser): void {
  sessionStorage.setItem(TOKEN_KEY, token)
  sessionStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function clearSession(): void {
  sessionStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(USER_KEY)
}
