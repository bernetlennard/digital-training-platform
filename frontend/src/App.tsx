import { Navigate, Route, Routes } from 'react-router'
import { RedirectIfAuthenticated, RequireAuth } from './components/RouteGuards'
import { HomePage } from './pages/HomePage'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'

function App() {
  return (
    <Routes>
      {/* Öffentlich */}
      <Route element={<RedirectIfAuthenticated />}>
        <Route path="/anmelden" element={<LoginPage />} />
        <Route path="/registrieren" element={<RegisterPage />} />
      </Route>

      {/* Nur mit Anmeldung (#NFA-7.7) */}
      <Route element={<RequireAuth />}>
        <Route path="/" element={<HomePage />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App
