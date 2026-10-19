import { request } from './client'
import type {
  MoveSetRequest,
  NoteRequest,
  PlannedSessionResponse,
  PlanSessionRequest,
  SessionExerciseResponse,
  SessionResponse,
  SetRequest,
  StartSessionRequest,
} from './types'

// ---------------------------------------------------------------------------
// Trainingsplanung
// ---------------------------------------------------------------------------

/** Geplante Trainings, das früheste zuerst (#FA-4.3) */
export function getPlannedSessions() {
  return request<PlannedSessionResponse[]>('/sessions/planned')
}

export function planSession(plan: PlanSessionRequest) {
  return request<PlannedSessionResponse>('/sessions/planned', {
    method: 'POST',
    body: plan,
  })
}

export function updatePlannedSession(
  sessionId: number,
  plan: PlanSessionRequest,
) {
  return request<PlannedSessionResponse>(`/sessions/planned/${sessionId}`, {
    method: 'PUT',
    body: plan,
  })
}

export function deletePlannedSession(sessionId: number) {
  return request<void>(`/sessions/planned/${sessionId}`, { method: 'DELETE' })
}

// ---------------------------------------------------------------------------
// Live-Training
// ---------------------------------------------------------------------------

/** Meldungen, wenn ein Training nicht starten kann (#FA-5.1) */
export const START_ERROR_MESSAGES = {
  404: 'Die Routine oder das Training wurde nicht gefunden.',
  409: 'Es läuft bereits ein Training. Setze es fort oder brich es ab.',
}

export function startPlannedSession(sessionId: number) {
  return request<SessionResponse>(`/sessions/planned/${sessionId}/start`, {
    method: 'POST',
  })
}

/** Startet eine Routine ohne vorherige Planung */
export function startSpontaneousSession(start: StartSessionRequest) {
  return request<SessionResponse>('/sessions', { method: 'POST', body: start })
}

/** Aktive Einheit oder null, wenn keine läuft (204) */
export async function getActiveSession() {
  const session = await request<SessionResponse | undefined>('/sessions/active')
  return session ?? null
}

export function updateNote(note: NoteRequest) {
  return request<void>('/sessions/active/note', { method: 'PUT', body: note })
}

/** Antwortet mit der ganzen Übung: alle Sätze und die noch offenen Vorschläge */
export function addSet(sessionExerciseId: number, set: SetRequest) {
  return request<SessionExerciseResponse>(
    `/sessions/active/exercises/${sessionExerciseId}/sets`,
    { method: 'POST', body: set },
  )
}

export function updateSet(setId: number, set: SetRequest) {
  return request<SessionExerciseResponse>(`/sessions/active/sets/${setId}`, {
    method: 'PUT',
    body: set,
  })
}

export function moveSet(setId: number, move: MoveSetRequest) {
  return request<SessionExerciseResponse>(
    `/sessions/active/sets/${setId}/position`,
    { method: 'PUT', body: move },
  )
}

export function deleteSet(setId: number) {
  return request<SessionExerciseResponse>(`/sessions/active/sets/${setId}`, {
    method: 'DELETE',
  })
}

/** 409 ohne erfassten Satz (#FA-5.7) */
export function finishSession() {
  return request<void>('/sessions/active/finish', { method: 'POST' })
}

/** Verwirft alle Sätze, ein geplantes Training bleibt eingeplant (#FA-5.8) */
export function cancelSession() {
  return request<void>('/sessions/active/cancel', { method: 'POST' })
}
