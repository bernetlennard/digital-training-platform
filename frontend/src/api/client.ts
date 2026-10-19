import type { ErrorResponse } from './types'

/** Status 0 heisst: Server nicht erreichbar */
export class ApiError extends Error {
  readonly status: number
  readonly details: string[]

  constructor(status: number, message: string, details: string[] = []) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.details = details
  }
}

let onUnauthorized: (() => void) | undefined

export function setUnauthorizedHandler(handler: (() => void) | undefined) {
  onUnauthorized = handler
}

export interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  body?: unknown
  checksPassword?: boolean
}

// Frontend und API laufen unter derselben Adresse, darum schickt fetch das Cookie ohne credentials-Option mit
export async function request<T>(
  path: string,
  options: RequestOptions = {},
): Promise<T> {
  const { method = 'GET', body, checksPassword = false } = options
  const headers: Record<string, string> = { Accept: 'application/json' }
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }

  let response: Response
  try {
    response = await fetch(`/api/v1${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    })
  } catch {
    throw new ApiError(0, 'Server nicht erreichbar')
  }

  if (!response.ok) {
    const error = await readError(response)
    // Beim Login und bei der Passwortbestätigung heisst 401 «Passwort falsch», nicht «Sitzung abgelaufen»
    if (error.status === 401 && !checksPassword && !path.startsWith('/auth/')) {
      onUnauthorized?.()
    }
    throw error
  }

  if (response.status === 204) {
    return undefined as T
  }
  return (await response.json()) as T
}

async function readError(response: Response): Promise<ApiError> {
  try {
    const body = (await response.json()) as ErrorResponse
    return new ApiError(response.status, body.message, body.details ?? [])
  } catch {
    // Kein JSON, z. B. wenn der Proxy das Backend nicht erreicht
    return new ApiError(response.status, response.statusText)
  }
}

export function parseId(param: string | undefined): number {
  const id = Number(param)
  if (!Number.isSafeInteger(id) || id < 1) {
    throw new ApiError(404, 'Not found')
  }
  return id
}

// Die message des Backends ist englisch und technisch, darum zählt nur der Statuscode
export function getErrorMessage(
  error: unknown,
  byStatus: Record<number, string> = {},
): string {
  if (error instanceof ApiError) {
    if (byStatus[error.status]) {
      return byStatus[error.status]
    }
    if (error.status === 0 || error.status >= 502) {
      return 'Der Server ist nicht erreichbar. Bitte versuche es später erneut.'
    }
    if (error.status === 400) {
      return 'Bitte überprüfe deine Eingaben.'
    }
    if (error.status === 401) {
      return 'Deine Sitzung ist abgelaufen. Bitte melde dich erneut an.'
    }
    if (error.status === 404) {
      return 'Der Eintrag wurde nicht gefunden. Vielleicht wurde er inzwischen gelöscht.'
    }
  }
  return 'Es ist ein unerwarteter Fehler aufgetreten. Bitte versuche es erneut.'
}
