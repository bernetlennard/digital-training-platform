import { Navigate, Outlet, useLocation, type Location } from 'react-router'
import { useAuth } from '../auth/useAuth'
import { LoadingScreen } from './LoadingScreen'

interface RedirectState {
  from?: Location
}

/** Schützt die angemeldeten Bereiche (#NFA-7.7) und merkt sich die gewünschte Ansicht. */
export function RequireAuth() {
  const { user, isLoading } = useAuth()
  const location = useLocation()

  if (isLoading) {
    return <LoadingScreen />
  }
  if (!user) {
    return (
      <Navigate
        to="/anmelden"
        replace
        state={{ from: location } satisfies RedirectState}
      />
    )
  }
  return <Outlet />
}

/**
 * Für Anmelden und Registrieren: Wer angemeldet ist, kommt zur ursprünglichen Ansicht zurück.
 * Damit leitet auch ein erfolgreicher Login oder eine Registrierung automatisch weiter.
 */
export function RedirectIfAuthenticated() {
  const { user, isLoading } = useAuth()
  const location = useLocation()

  if (isLoading) {
    return <LoadingScreen />
  }
  if (user) {
    const from = (location.state as RedirectState | null)?.from
    return <Navigate to={from ?? '/'} replace />
  }
  return <Outlet />
}
