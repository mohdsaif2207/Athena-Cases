import axios from 'axios'
import { clearSession, getAccessToken } from '@/lib/auth-storage'

/**
 * Shared HTTP client. Base URL is empty so Vite's `/api` proxy is used in dev.
 * Override with `VITE_API_BASE_URL` when the UI is served against a remote API.
 */
const baseURL = import.meta.env.VITE_API_BASE_URL ?? ''

export const apiClient = axios.create({
  baseURL,
  timeout: 15_000,
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
  },
})

apiClient.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const url = String(error.config?.url ?? '')
      if (!url.includes('/api/v1/auth/login')) {
        clearSession()
        if (window.location.pathname !== '/login') {
          window.location.assign('/login')
        }
      }
    }
    return Promise.reject(error)
  },
)
