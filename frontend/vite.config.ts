/// <reference types="vitest/config" />
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // Frontend und API laufen lokal unter derselben Adresse wie später hinter Caddy,
    // damit das SameSite=Strict-Cookie ohne CORS mitgeschickt wird (#NFA-7.18)
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
  test: {
    environment: 'jsdom',
    setupFiles: './src/setupTests.ts',
    // Feste Zeitzone wie auf dem Smartphone in der Schweiz, damit Datum und Uhrzeit
    // auch in der Pipeline (UTC) gleich formatiert werden
    env: { TZ: 'Europe/Zurich' },
  },
})
