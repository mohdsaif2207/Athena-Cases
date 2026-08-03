import axios from 'axios'

/**
 * Shared HTTP client. Base URL is empty so Vite's `/api` proxy is used in dev.
 * Override with `VITE_API_BASE_URL` when the UI is served against a remote API.
 */
export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '',
  timeout: 15_000,
  headers: { Accept: 'application/json' },
})
