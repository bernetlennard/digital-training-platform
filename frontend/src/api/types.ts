// DTOs gemäss REST-Schnittstelle (Kapitel «REST-Schnittstelle»)

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

// Registrieren und Anmelden verwenden denselben Aufbau
export interface CredentialsRequest {
  email: string
  password: string
}
