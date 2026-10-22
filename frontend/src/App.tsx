import { Navigate, Route, Routes } from 'react-router'
import { AppLayout } from './components/AppLayout'
import { RedirectIfAuthenticated, RequireAuth } from './components/RouteGuards'
import { AccountPage } from './pages/AccountPage'
import { ExercisesPage } from './pages/ExercisesPage'
import { GuidePage } from './pages/GuidePage'
import { HistoryPage } from './pages/HistoryPage'
import { LiveTrainingPage } from './pages/LiveTrainingPage'
import { LoginPage } from './pages/LoginPage'
import { PrivacyPage } from './pages/PrivacyPage'
import { RegisterPage } from './pages/RegisterPage'
import { RoutineEditPage } from './pages/RoutineEditPage'
import { RoutineHistoryPage } from './pages/RoutineHistoryPage'
import { RoutinesPage } from './pages/RoutinesPage'
import { SessionDetailPage } from './pages/SessionDetailPage'
import { StartPage } from './pages/StartPage'

function App() {
  return (
    <Routes>
      <Route element={<RedirectIfAuthenticated />}>
        <Route path="/anmelden" element={<LoginPage />} />
        <Route path="/registrieren" element={<RegisterPage />} />
      </Route>
      <Route path="/datenschutz" element={<PrivacyPage />} />
      <Route path="/anleitung" element={<GuidePage />} />

      <Route element={<RequireAuth />}>
        <Route element={<AppLayout />}>
          <Route path="/" element={<StartPage />} />
          <Route path="/routinen" element={<RoutinesPage />} />
          <Route path="/routinen/:routineId" element={<RoutineEditPage />} />
          <Route path="/uebungen" element={<ExercisesPage />} />
          <Route path="/verlauf" element={<HistoryPage />} />
          <Route path="/verlauf/:routineId" element={<RoutineHistoryPage />} />
          <Route
            path="/verlauf/einheit/:sessionId"
            element={<SessionDetailPage />}
          />
          <Route path="/konto" element={<AccountPage />} />
        </Route>
        <Route path="/training" element={<LiveTrainingPage />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App
