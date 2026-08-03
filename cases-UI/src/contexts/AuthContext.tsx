import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { fetchCurrentUser, loginRequest } from '@/api/auth'
import type { AuthenticatedUser } from '@/api/types'
import { clearSession, getAccessToken, getStoredUser, persistSession } from '@/lib/auth-storage'

interface AuthContextValue {
  user: AuthenticatedUser | null
  isAuthenticated: boolean
  login: (username: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthenticatedUser | null>(() =>
    getAccessToken() ? getStoredUser() : null,
  )

  // Refresh profile from DB so display_name stays current (not username).
  useEffect(() => {
    const token = getAccessToken()
    if (!token) {
      return
    }

    let cancelled = false
    void fetchCurrentUser()
      .then((profile) => {
        if (cancelled) return
        persistSession(token, profile)
        setUser(profile)
      })
      .catch(() => {
        if (cancelled) return
        clearSession()
        setUser(null)
      })

    return () => {
      cancelled = true
    }
  }, [])

  const login = useCallback(async (username: string, password: string) => {
    const response = await loginRequest(username, password)
    persistSession(response.accessToken, response.user)
    setUser(response.user)
  }, [])

  const logout = useCallback(() => {
    clearSession()
    setUser(null)
  }, [])

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: Boolean(user && getAccessToken()),
      login,
      logout,
    }),
    [user, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return ctx
}
