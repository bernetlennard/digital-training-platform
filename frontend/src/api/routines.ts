import { request } from './client'
import type { RoutineRequest, RoutineResponse } from './types'

export function getRoutines() {
  return request<RoutineResponse[]>('/routines')
}

export function getRoutine(routineId: number) {
  return request<RoutineResponse>(`/routines/${routineId}`)
}

export function createRoutine(routine: RoutineRequest) {
  return request<RoutineResponse>('/routines', {
    method: 'POST',
    body: routine,
  })
}

export function updateRoutine(routineId: number, routine: RoutineRequest) {
  return request<RoutineResponse>(`/routines/${routineId}`, {
    method: 'PUT',
    body: routine,
  })
}

export function deleteRoutine(routineId: number) {
  return request<void>(`/routines/${routineId}`, { method: 'DELETE' })
}
