import { createContext } from 'react'
import type {
  CredentialsRequest,
  UpdateProfileRequest,
  UserResponse,
} from '../api/types'

export interface AuthContextValue {
  /** Angemeldeter Sportler oder null, wenn niemand angemeldet ist */
  user: UserResponse | null
  /** true, solange beim Laden der App noch geprüft wird, ob eine Sitzung besteht */
  isLoading: boolean
  login: (credentials: CredentialsRequest) => Promise<void>
  register: (
    credentials: CredentialsRequest,
    profile: UpdateProfileRequest,
  ) => Promise<void>
  logout: () => Promise<void>
  /** Übernimmt geänderte Kontodaten oder null, nachdem das Konto gelöscht wurde */
  setUser: (user: UserResponse | null) => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)
