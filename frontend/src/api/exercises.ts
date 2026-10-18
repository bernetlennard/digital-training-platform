import { request } from './client'
import type {
  ExerciseRequest,
  ExerciseResponse,
  MuscleGroupResponse,
} from './types'

/** Eigene Übungen ohne die gelöschten */
export function getExercises() {
  return request<ExerciseResponse[]>('/exercises')
}

/** Feste Liste der Muskelgruppen (#FA-2.2) */
export function getMuscleGroups() {
  return request<MuscleGroupResponse[]>('/exercises/muscle-groups')
}

export function createExercise(exercise: ExerciseRequest) {
  return request<ExerciseResponse>('/exercises', {
    method: 'POST',
    body: exercise,
  })
}

export function updateExercise(exerciseId: number, exercise: ExerciseRequest) {
  return request<ExerciseResponse>(`/exercises/${exerciseId}`, {
    method: 'PUT',
    body: exercise,
  })
}

/** 409, solange die Übung in einer Routine vorkommt (#FA-2.6) */
export function deleteExercise(exerciseId: number) {
  return request<void>(`/exercises/${exerciseId}`, { method: 'DELETE' })
}
