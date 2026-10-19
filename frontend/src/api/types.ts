export interface ErrorResponse {
  status: number
  message: string
  details: string[]
}

export interface UserResponse {
  id: number
  email: string
  firstName: string | null
  lastName: string | null
}

export interface CredentialsRequest {
  email: string
  password: string
}

export interface UpdateProfileRequest {
  firstName: string | null
  lastName: string | null
}

export interface ChangeEmailRequest {
  newEmail: string
  password: string
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
}

export interface DeleteAccountRequest {
  password: string
}

export interface MuscleGroupResponse {
  id: number
  name: string
}

export interface ExerciseRequest {
  name: string
  muscleGroupId: number
  description: string | null
}

export interface ExerciseResponse {
  id: number
  name: string
  muscleGroupId: number
  muscleGroupName: string
  description: string | null
}

export interface RoutineRequest {
  name: string
  /** Reihenfolge = Position in der Routine */
  exerciseIds: number[]
}

export interface RoutineExerciseResponse {
  position: number
  exerciseId: number
  name: string
  muscleGroupName: string
}

export interface RoutineResponse {
  id: number
  name: string
  exercises: RoutineExerciseResponse[]
}

/** Datum ohne Zeit im Format JJJJ-MM-TT (LocalDate) */
export type IsoDate = string
/** Zeitpunkt im Format ISO 8601 in UTC (Instant) */
export type IsoInstant = string

export interface PlanSessionRequest {
  routineId: number
  plannedDate: IsoDate
}

export interface PlannedSessionResponse {
  id: number
  routineId: number
  routineName: string
  plannedDate: IsoDate
}

export interface StartSessionRequest {
  routineId: number
}

export interface NoteRequest {
  note: string | null
}

export interface SetRequest {
  weightKg: number
  repetitions: number
  restSeconds: number | null
}

export interface MoveSetRequest {
  newSetNumber: number
}

export interface SetResponse {
  id: number
  setNumber: number
  weightKg: number
  repetitions: number
  restSeconds: number | null
}

/** Offener Satz aus der letzten Einheit derselben Routine, nicht gespeichert */
export interface SetSuggestion {
  setNumber: number
  weightKg: number
  repetitions: number
  restSeconds: number | null
}

export interface SessionExerciseResponse {
  id: number
  exerciseId: number
  exerciseName: string
  position: number
  sets: SetResponse[]
  suggestedSets: SetSuggestion[]
}

export type SessionStatus = 'geplant' | 'aktiv' | 'abgeschlossen'

export interface SessionResponse {
  id: number
  routineId: number
  routineName: string
  routineDeleted: boolean
  status: SessionStatus
  startedAt: IsoInstant | null
  finishedAt: IsoInstant | null
  note: string | null
  exercises: SessionExerciseResponse[]
}

export interface HistoryRoutineResponse {
  routineId: number
  name: string
  deleted: boolean
}

export interface SessionSummaryResponse {
  id: number
  startedAt: IsoInstant
  finishedAt: IsoInstant
}
