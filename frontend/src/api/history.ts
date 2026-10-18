import { request } from './client'
import type {
  HistoryRoutineResponse,
  SessionResponse,
  SessionSummaryResponse,
} from './types'

/** Routinen mit mindestens einer abgeschlossenen Einheit, auch gelöschte (#FA-6.3) */
export function getHistoryRoutines() {
  return request<HistoryRoutineResponse[]>('/history/routines')
}

/** Abgeschlossene Einheiten einer Routine, die neueste zuerst (#FA-6.1) */
export function getRoutineHistory(routineId: number) {
  return request<SessionSummaryResponse[]>(
    `/history/routines/${routineId}/sessions`,
  )
}

export function getHistorySession(sessionId: number) {
  return request<SessionResponse>(`/history/sessions/${sessionId}`)
}

/** Soft Delete, die Einheit verschwindet aus dem Verlauf (#FA-6.4) */
export function deleteHistorySession(sessionId: number) {
  return request<void>(`/history/sessions/${sessionId}`, { method: 'DELETE' })
}
