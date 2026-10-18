import { getCurrentUser } from './auth'
import { ApiError, request, type RequestOptions } from './client'
import type {
  ChangeEmailRequest,
  ChangePasswordRequest,
  DeleteAccountRequest,
  UpdateProfileRequest,
  UserResponse,
} from './types'

/** Ändert Vor- und Nachname. */
export function updateProfile(profile: UpdateProfileRequest) {
  return request<UserResponse>('/users/me', { method: 'PUT', body: profile })
}

/** Ändert die E-Mail-Adresse. Der Server setzt dabei ein neues Auth-Cookie. */
export function changeEmail(change: ChangeEmailRequest) {
  return requestWithPassword<UserResponse>('/users/me/email', {
    method: 'PUT',
    body: change,
  })
}

/** Ändert das Passwort. Der Server setzt dabei ein neues Auth-Cookie. */
export function changePassword(change: ChangePasswordRequest) {
  return requestWithPassword<void>('/users/me/password', {
    method: 'PUT',
    body: change,
  })
}

/** Löscht das Konto mit allen Daten. Der Server löscht das Auth-Cookie. */
export function deleteAccount(confirmation: DeleteAccountRequest) {
  return requestWithPassword<void>('/users/me', {
    method: 'DELETE',
    body: confirmation,
  })
}

/**
 * Für Endpunkte mit Passwortbestätigung. Ein 401 kann hier «Passwort falsch» oder «Sitzung
 * abgelaufen» heissen. Eine zweite Anfrage klärt das: Ist die Sitzung abgelaufen, führt deren
 * 401 wie überall zurück zur Anmeldung.
 */
async function requestWithPassword<T>(path: string, options: RequestOptions) {
  try {
    return await request<T>(path, { ...options, checksPassword: true })
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) {
      await getCurrentUser()
    }
    throw error
  }
}
