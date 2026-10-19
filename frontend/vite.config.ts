/// <reference types="vitest/config" />
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [react()],
  server: {
    // Gleiche Adresse für Frontend und API, sonst schickt der Browser das SameSite=Strict-Cookie nicht mit
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
  test: {
    environment: 'jsdom',
    setupFiles: './src/setupTests.ts',
    // Feste Zeitzone, damit die Formate auch in der Pipeline (UTC) gleich sind
    env: { TZ: 'Europe/Zurich' },
  },
})
