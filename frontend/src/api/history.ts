import { request } from './client'
import type {
  HistoryRoutineResponse,
  SessionResponse,
  SessionSummaryResponse,
} from './types'

export function getHistoryRoutines() {
  return request<HistoryRoutineResponse[]>('/history/routines')
}

export function getRoutineHistory(routineId: number) {
  return request<SessionSummaryResponse[]>(
    `/history/routines/${routineId}/sessions`,
  )
}

export function getHistorySession(sessionId: number) {
  return request<SessionResponse>(`/history/sessions/${sessionId}`)
}

export function deleteHistorySession(sessionId: number) {
  return request<void>(`/history/sessions/${sessionId}`, { method: 'DELETE' })
}
