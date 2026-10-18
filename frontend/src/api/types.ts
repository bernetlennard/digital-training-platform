// DTOs gemäss REST-Schnittstelle (Kapitel «REST-Schnittstelle»)

export interface ErrorResponse {
  status: number
  message: string
  details: string[]
}

// ---------------------------------------------------------------------------
// Benutzerkonto
// ---------------------------------------------------------------------------

export interface UserResponse {
  id: number
  email: string
  firstName: string | null
  lastName: string | null
}

// Registrieren und Anmelden verwenden denselben Aufbau
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

// ---------------------------------------------------------------------------
// Übungen
// ---------------------------------------------------------------------------

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

// ---------------------------------------------------------------------------
// Routinen
// ---------------------------------------------------------------------------

export interface RoutineRequest {
  name: string
  /** Reihenfolge der Liste = Position in der Routine */
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

// ---------------------------------------------------------------------------
// Trainingsplanung und Live-Training
// ---------------------------------------------------------------------------

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

/** Vorbelegung für den nächsten Satz (#FA-5.5) */
export interface SetSuggestion {
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
  nextSet: SetSuggestion | null
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

// ---------------------------------------------------------------------------
// Verlauf
// ---------------------------------------------------------------------------

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
