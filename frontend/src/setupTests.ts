import '@testing-library/jest-dom/vitest'
import { cleanup } from '@testing-library/react'
import { afterEach } from 'vitest'

// Ohne Vitest-Globals räumt die Testing Library das DOM nicht selbst auf
afterEach(() => {
  cleanup()
})
