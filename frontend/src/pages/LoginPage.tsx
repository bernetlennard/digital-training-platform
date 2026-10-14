import {
  Alert,
  Button,
  Link,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link as RouterLink } from 'react-router'
import { getErrorMessage } from '../api/client'
import type { CredentialsRequest } from '../api/types'
import { useAuth } from '../auth/useAuth'
import { AuthLayout } from '../components/AuthLayout'
import { emailRules, textFieldProps } from '../components/formFields'
import { PasswordField } from '../components/PasswordField'

export function LoginPage() {
  const { login } = useAuth()
  const [submitError, setSubmitError] = useState<string>()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<CredentialsRequest>({
    defaultValues: { email: '', password: '' },
  })

  const onSubmit = async (credentials: CredentialsRequest) => {
    setSubmitError(undefined)
    try {
      // Die Weiterleitung übernimmt RedirectIfAuthenticated, sobald der Sportler gesetzt ist
      await login(credentials)
    } catch (error) {
      setSubmitError(
        getErrorMessage(error, {
          // Bewusst allgemein: verrät nicht, ob die E-Mail-Adresse registriert ist (#NFA-1.4)
          401: 'E-Mail-Adresse oder Passwort ist falsch.',
          429: 'Zu viele Fehlversuche. Die Anmeldung ist für 15 Minuten gesperrt.',
        }),
      )
    }
  }

  return (
    <AuthLayout title="Anmelden">
      <form noValidate onSubmit={handleSubmit(onSubmit)}>
        <Stack spacing={2}>
          {submitError && <Alert severity="error">{submitError}</Alert>}
          <TextField
            {...textFieldProps(register('email', emailRules))}
            label="E-Mail"
            type="email"
            autoComplete="email"
            required
            fullWidth
            error={!!errors.email}
            helperText={errors.email?.message}
          />
          <PasswordField
            {...textFieldProps(
              register('password', { required: 'Bitte Passwort eingeben.' }),
            )}
            label="Passwort"
            autoComplete="current-password"
            error={!!errors.password}
            helperText={errors.password?.message}
          />
          <Button
            type="submit"
            variant="contained"
            size="large"
            fullWidth
            loading={isSubmitting}
          >
            Anmelden
          </Button>
          <Typography variant="body2" sx={{ textAlign: 'center' }}>
            Noch kein Konto?{' '}
            <Link component={RouterLink} to="/registrieren">
              Registrieren
            </Link>
          </Typography>
        </Stack>
      </form>
    </AuthLayout>
  )
}
