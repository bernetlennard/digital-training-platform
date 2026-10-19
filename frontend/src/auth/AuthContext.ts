import { createContext } from 'react'
import type {
  CredentialsRequest,
  UpdateProfileRequest,
  UserResponse,
} from '../api/types'

export interface AuthContextValue {
  user: UserResponse | null
  /** true, solange beim Start der App noch geprüft wird, ob eine Sitzung besteht */
  isLoading: boolean
  login: (credentials: CredentialsRequest) => Promise<void>
  register: (
    credentials: CredentialsRequest,
    profile: UpdateProfileRequest,
  ) => Promise<void>
  logout: () => Promise<void>
  setUser: (user: UserResponse | null) => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)
