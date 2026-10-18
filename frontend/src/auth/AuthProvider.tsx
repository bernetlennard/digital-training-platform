import { useEffect, useMemo, useState, type ReactNode } from 'react'
import * as authApi from '../api/auth'
import { setUnauthorizedHandler } from '../api/client'
import type { UserResponse } from '../api/types'
import * as usersApi from '../api/users'
import { AuthContext, type AuthContextValue } from './AuthContext'

/**
 * Hält fest, wer angemeldet ist. Das Token selbst bleibt im HttpOnly-Cookie und ist hier nie sichtbar.
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserResponse | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  // Beim Laden fragen, ob das Cookie noch eine gültige Sitzung enthält
  useEffect(() => {
    let ignore = false
    authApi
      .getCurrentUser()
      .then((currentUser) => {
        if (!ignore) setUser(currentUser)
      })
      .catch(() => {
        // 401 oder Server nicht erreichbar: als nicht angemeldet behandeln
        if (!ignore) setUser(null)
      })
      .finally(() => {
        if (!ignore) setIsLoading(false)
      })
    return () => {
      ignore = true
    }
  }, [])

  // Läuft das Token während der Benutzung ab, führt der nächste 401 zurück zur Anmeldung
  useEffect(() => {
    setUnauthorizedHandler(() => setUser(null))
    return () => setUnauthorizedHandler(undefined)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isLoading,
      login: async (credentials) => {
        setUser(await authApi.login(credentials))
      },
      register: async (credentials, profile) => {
        let registered = await authApi.register(credentials)
        // Die Registrierung kennt nur E-Mail und Passwort. Vor- und Nachname gehen gleich danach
        // ans Profil, das Auth-Cookie ist zu diesem Zeitpunkt schon gesetzt.
        if (profile.firstName || profile.lastName) {
          try {
            registered = await usersApi.updateProfile(profile)
          } catch {
            // Das Konto besteht bereits, die Namen lassen sich unter «Konto» nachtragen
          }
        }
        setUser(registered)
      },
      logout: async () => {
        await authApi.logout()
        setUser(null)
      },
      setUser,
    }),
    [user, isLoading],
  )

  return <AuthContext value={value}>{children}</AuthContext>
}
