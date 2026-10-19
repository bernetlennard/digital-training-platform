import { request } from './client'
import type { CredentialsRequest, UserResponse } from './types'

export function register(credentials: CredentialsRequest) {
  return request<UserResponse>('/auth/register', {
    method: 'POST',
    body: credentials,
  })
}

export function login(credentials: CredentialsRequest) {
  return request<UserResponse>('/auth/login', {
    method: 'POST',
    body: credentials,
  })
}

export function logout() {
  return request<void>('/auth/logout', { method: 'POST' })
}

// Das Cookie ist für JavaScript unsichtbar: 200 heisst angemeldet, 401 nicht angemeldet
export function getCurrentUser() {
  return request<UserResponse>('/users/me')
}
