import { Navigate, Outlet, useLocation, type Location } from 'react-router'
import { useAuth } from '../auth/useAuth'
import { LoadingScreen } from './LoadingScreen'

interface RedirectState {
  from?: Location
}

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

// Leitet auch nach erfolgreichem Login oder Registrieren zur ursprünglichen Ansicht weiter
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
