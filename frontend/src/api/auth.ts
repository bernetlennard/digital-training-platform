import { request } from './client'
import type { CredentialsRequest, UserResponse } from './types'

/** Legt ein Konto an und meldet gleich an. Der Server setzt das Auth-Cookie. */
export function register(credentials: CredentialsRequest) {
  return request<UserResponse>('/auth/register', {
    method: 'POST',
    body: credentials,
  })
}

/** Meldet an. Der Server setzt das Auth-Cookie. */
export function login(credentials: CredentialsRequest) {
  return request<UserResponse>('/auth/login', {
    method: 'POST',
    body: credentials,
  })
}

/** Meldet ab. Der Server löscht das Auth-Cookie mit Max-Age=0. */
export function logout() {
  return request<void>('/auth/logout', { method: 'POST' })
}

/** Das Frontend kann das Cookie nicht lesen: 200 heisst angemeldet, 401 nicht angemeldet. */
export function getCurrentUser() {
  return request<UserResponse>('/users/me')
}
