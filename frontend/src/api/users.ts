import { getCurrentUser } from './auth'
import { ApiError, request, type RequestOptions } from './client'
import type {
  ChangeEmailRequest,
  ChangePasswordRequest,
  DeleteAccountRequest,
  UpdateProfileRequest,
  UserResponse,
} from './types'

export function updateProfile(profile: UpdateProfileRequest) {
  return request<UserResponse>('/users/me', { method: 'PUT', body: profile })
}

export function changeEmail(change: ChangeEmailRequest) {
  return requestWithPassword<UserResponse>('/users/me/email', {
    method: 'PUT',
    body: change,
  })
}

export function changePassword(change: ChangePasswordRequest) {
  return requestWithPassword<void>('/users/me/password', {
    method: 'PUT',
    body: change,
  })
}

export function deleteAccount(confirmation: DeleteAccountRequest) {
  return requestWithPassword<void>('/users/me', {
    method: 'DELETE',
    body: confirmation,
  })
}

// 401 heisst hier «Passwort falsch» oder «Sitzung abgelaufen». Ist die Sitzung abgelaufen,
// meldet die zweite Anfrage wie überall ab.
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
