import { request } from './client'
import type {
  ExerciseRequest,
  ExerciseResponse,
  MuscleGroupResponse,
} from './types'

export function getExercises() {
  return request<ExerciseResponse[]>('/exercises')
}

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

export function deleteExercise(exerciseId: number) {
  return request<void>(`/exercises/${exerciseId}`, { method: 'DELETE' })
}
