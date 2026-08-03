import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  // Proxy target must match VITE_API_BASE_URL / cases-MT (8090). Absolute axios baseURL bypasses proxy.
  const apiTarget = env.VITE_API_BASE_URL?.trim() || 'http://localhost:8090'

  return {
    plugins: [react()],
    server: {
      proxy: {
        '/api': {
          target: apiTarget,
          changeOrigin: true,
        },
      },
    },
  }
})
