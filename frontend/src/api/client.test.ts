import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import {
  ApiError,
  getErrorMessage,
  parseId,
  request,
  setUnauthorizedHandler,
} from './client'

const UNREACHABLE =
  'Der Server ist nicht erreichbar. Bitte versuche es später erneut.'
const UNEXPECTED =
  'Es ist ein unerwarteter Fehler aufgetreten. Bitte versuche es erneut.'

function jsonResponse(status: number, body: unknown) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

describe('UTF-03 Fehlermeldungen je Statuscode', () => {
  it.each([
    [400, 'Bitte überprüfe deine Eingaben.'],
    [401, 'Deine Sitzung ist abgelaufen. Bitte melde dich erneut an.'],
    [
      404,
      'Der Eintrag wurde nicht gefunden. Vielleicht wurde er inzwischen gelöscht.',
    ],
    [0, UNREACHABLE],
    [502, UNREACHABLE],
    [503, UNREACHABLE],
    [409, UNEXPECTED],
    [500, UNEXPECTED],
  ])('Status %i ergibt eine deutsche Meldung', (status, message) => {
    expect(getErrorMessage(new ApiError(status, 'Technical message'))).toBe(
      message,
    )
  })

  it('die Meldung der Ansicht hat Vorrang, auch ohne Verbindung', () => {
    const byStatus = {
      0: 'Satz noch nicht gespeichert.',
      409: 'Name vergeben.',
    }
    expect(getErrorMessage(new ApiError(409, 'Conflict'), byStatus)).toBe(
      'Name vergeben.',
    )
    expect(getErrorMessage(new ApiError(0, 'Offline'), byStatus)).toBe(
      'Satz noch nicht gespeichert.',
    )
  })

  it('die englische Meldung des Backends erscheint nie', () => {
    const message = getErrorMessage(
      new ApiError(409, 'Exercise name already exists'),
    )
    expect(message).not.toContain('Exercise')
  })

  it('andere Fehler ergeben die allgemeine Meldung', () => {
    expect(getErrorMessage(new TypeError('x is undefined'))).toBe(UNEXPECTED)
  })
})

describe('UTF-04 Anfragen an die REST-Schnittstelle', () => {
  const fetchMock = vi.fn<typeof fetch>()
  const onUnauthorized = vi.fn()

  beforeEach(() => {
    vi.stubGlobal('fetch', fetchMock)
    setUnauthorizedHandler(onUnauthorized)
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    fetchMock.mockReset()
    onUnauthorized.mockReset()
    setUnauthorizedHandler(undefined)
  })

  it('schickt den Body als JSON an /api/v1 und liefert die Antwort', async () => {
    fetchMock.mockResolvedValue(jsonResponse(201, { id: 7 }))

    const result = await request('/exercises', {
      method: 'POST',
      body: { name: 'Bankdrücken' },
    })

    expect(result).toEqual({ id: 7 })
    expect(fetchMock).toHaveBeenCalledWith('/api/v1/exercises', {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
      body: '{"name":"Bankdrücken"}',
    })
  })

  it('eine Antwort ohne Inhalt (204) ergibt undefined', async () => {
    fetchMock.mockResolvedValue(new Response(null, { status: 204 }))

    await expect(
      request('/sessions/active/finish', { method: 'POST' }),
    ).resolves.toBeUndefined()
  })

  it('ein Fehler im Fehlerformat wird zum ApiError mit Details', async () => {
    fetchMock.mockResolvedValue(
      jsonResponse(400, {
        status: 400,
        message: 'Validation failed',
        details: ['name: must not be blank'],
      }),
    )

    const error = await request('/exercises').catch((e: unknown) => e)

    expect(error).toBeInstanceOf(ApiError)
    expect(error).toMatchObject({
      status: 400,
      details: ['name: must not be blank'],
    })
  })

  it('ein Fehler ohne JSON behält den Statuscode', async () => {
    fetchMock.mockResolvedValue(
      new Response('<html>Bad Gateway</html>', {
        status: 502,
        statusText: 'Bad Gateway',
      }),
    )

    await expect(request('/exercises')).rejects.toMatchObject({ status: 502 })
  })

  it('ohne Verbindung zum Server gibt es Status 0', async () => {
    fetchMock.mockRejectedValue(new TypeError('Failed to fetch'))

    await expect(request('/exercises')).rejects.toMatchObject({ status: 0 })
  })

  it('401 bei einer normalen Anfrage meldet ab (Sitzung abgelaufen)', async () => {
    fetchMock.mockResolvedValue(
      jsonResponse(401, { status: 401, message: 'Unauthorized', details: [] }),
    )

    await expect(request('/users/me')).rejects.toMatchObject({ status: 401 })
    expect(onUnauthorized).toHaveBeenCalledOnce()
  })

  it('401 beim Login oder bei der Passwortbestätigung meldet nicht ab', async () => {
    fetchMock.mockImplementation(() =>
      Promise.resolve(
        jsonResponse(401, {
          status: 401,
          message: 'Bad credentials',
          details: [],
        }),
      ),
    )

    await expect(
      request('/auth/login', { method: 'POST', body: {} }),
    ).rejects.toMatchObject({ status: 401 })
    await expect(
      request('/users/me', {
        method: 'DELETE',
        body: { password: 'x' },
        checksPassword: true,
      }),
    ).rejects.toMatchObject({ status: 401 })
    expect(onUnauthorized).not.toHaveBeenCalled()
  })
})

describe('UTF-05 IDs aus der Adresse', () => {
  it('eine positive ganze Zahl ist eine gültige ID', () => {
    expect(parseId('42')).toBe(42)
  })

  it.each([undefined, '', 'abc', '0', '-1', '1.5', '9007199254740993'])(
    'ID «%s» wird wie ein fehlender Eintrag behandelt (404)',
    (param) => {
      expect(() => parseId(param)).toThrow(
        expect.objectContaining({ status: 404 }),
      )
    },
  )
})
