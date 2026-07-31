import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const apiTarget =
    env.VITE_API_BASE_URL || env.VITE_API_URL || 'http://localhost:8081'

  return {
    plugins: [react()],
    // Optional same-origin proxy when VITE_API_BASE_URL is empty / relative.
    // Direct absolute baseURL (http://localhost:8081) bypasses this and needs CORS.
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
