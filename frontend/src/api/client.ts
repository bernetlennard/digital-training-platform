import type { ErrorResponse } from './types'

/** Fehler einer API-Anfrage im einheitlichen Fehlerformat. Status 0 heisst: Server nicht erreichbar. */
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

/** Wird bei einem 401 aufgerufen, d. h. das Token fehlt oder ist abgelaufen. */
export function setUnauthorizedHandler(handler: (() => void) | undefined) {
  onUnauthorized = handler
}

export interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  body?: unknown
  /** Der Endpunkt prüft das Passwort: 401 heisst «Passwort falsch», nicht «Sitzung abgelaufen» */
  checksPassword?: boolean
}

/**
 * Schickt eine Anfrage an die REST-Schnittstelle unter /api.
 *
 * Das Auth-Cookie ist HttpOnly und für JavaScript unsichtbar. Der Browser schickt es selbst mit,
 * weil Frontend und API unter derselben Adresse laufen (lokal über den Vite-Proxy, in Produktion
 * über Caddy). Der Standardwert credentials: 'same-origin' von fetch genügt deshalb.
 */
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
    // Beim Login bedeutet 401 «falsche Zugangsdaten», nicht «Sitzung abgelaufen»
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

/** Pfadparameter als ID. Ein ungültiger Wert wird wie ein fehlender Datensatz behandelt (404). */
export function parseId(param: string | undefined): number {
  const id = Number(param)
  if (!Number.isSafeInteger(id) || id < 1) {
    throw new ApiError(404, 'Not found')
  }
  return id
}

/**
 * Liefert eine deutsche Meldung für die Oberfläche (#NFA-7.4, #NFA-7.11).
 * Die message des Backends ist englisch und technisch, darum zählt nur der Statuscode.
 * Mit byStatus kann eine Ansicht für einzelne Statuscodes eine eigene Meldung festlegen.
 */
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
