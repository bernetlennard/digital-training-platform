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

export const START_ERROR_MESSAGES = {
  404: 'Die Routine oder das Training wurde nicht gefunden.',
  409: 'Es läuft bereits ein Training. Setze es fort oder brich es ab.',
}

export function startPlannedSession(sessionId: number) {
  return request<SessionResponse>(`/sessions/planned/${sessionId}/start`, {
    method: 'POST',
  })
}

export function startSpontaneousSession(start: StartSessionRequest) {
  return request<SessionResponse>('/sessions', { method: 'POST', body: start })
}

// 204 ohne Inhalt heisst: kein Training aktiv
export async function getActiveSession() {
  const session = await request<SessionResponse | undefined>('/sessions/active')
  return session ?? null
}

export function updateNote(note: NoteRequest) {
  return request<void>('/sessions/active/note', { method: 'PUT', body: note })
}

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

export function finishSession() {
  return request<void>('/sessions/active/finish', { method: 'POST' })
}

export function cancelSession() {
  return request<void>('/sessions/active/cancel', { method: 'POST' })
}
