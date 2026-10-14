import CheckCircle from '@mui/icons-material/CheckCircle'
import FitnessCenter from '@mui/icons-material/FitnessCenter'
import Logout from '@mui/icons-material/Logout'
import {
  Alert,
  AppBar,
  Box,
  Button,
  Stack,
  Toolbar,
  Typography,
} from '@mui/material'
import { useState } from 'react'
import { getErrorMessage } from '../api/client'
import { useAuth } from '../auth/useAuth'

export function HomePage() {
  const { user, logout } = useAuth()
  const [isLoggingOut, setIsLoggingOut] = useState(false)
  const [logoutError, setLogoutError] = useState<string>()

  const handleLogout = async () => {
    setLogoutError(undefined)
    setIsLoggingOut(true)
    try {
      // Danach leitet RequireAuth zur Anmeldung weiter
      await logout()
    } catch (error) {
      setLogoutError(getErrorMessage(error))
      setIsLoggingOut(false)
    }
  }

  return (
    <Box sx={{ minHeight: '100dvh', display: 'flex', flexDirection: 'column' }}>
      <AppBar position="static">
        <Toolbar>
          <FitnessCenter sx={{ mr: 1 }} />
          <Typography variant="h6" component="p">
            Trainingsplattform
          </Typography>
        </Toolbar>
      </AppBar>
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          p: 2,
        }}
      >
        <Stack spacing={2} sx={{ alignItems: 'center', textAlign: 'center' }}>
          <CheckCircle color="success" sx={{ fontSize: 64 }} />
          <Typography variant="h4" component="h1">
            Du bist angemeldet
          </Typography>
          <Typography color="text.secondary">
            Angemeldet als {user?.email}
          </Typography>
          {logoutError && <Alert severity="error">{logoutError}</Alert>}
          <Button
            variant="contained"
            size="large"
            startIcon={<Logout />}
            loading={isLoggingOut}
            onClick={handleLogout}
          >
            Abmelden
          </Button>
        </Stack>
      </Box>
    </Box>
  )
}
