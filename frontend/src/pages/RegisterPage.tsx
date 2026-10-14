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

const PASSWORD_HINT = '15 bis 64 Zeichen'

export function RegisterPage() {
  const { register: registerAccount } = useAuth()
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
      // Nach der Registrierung ist der Sportler direkt angemeldet (Cookie vom Server)
      await registerAccount(credentials)
    } catch (error) {
      setSubmitError(
        getErrorMessage(error, {
          409: 'Für diese E-Mail-Adresse besteht bereits ein Konto.',
        }),
      )
    }
  }

  return (
    <AuthLayout title="Registrieren">
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
              register('password', {
                required: 'Bitte Passwort eingeben.',
                minLength: {
                  value: 15,
                  message: `Das Passwort braucht ${PASSWORD_HINT}.`,
                },
                maxLength: {
                  value: 64,
                  message: `Das Passwort braucht ${PASSWORD_HINT}.`,
                },
              }),
            )}
            label="Passwort"
            autoComplete="new-password"
            error={!!errors.password}
            helperText={errors.password?.message ?? PASSWORD_HINT}
          />
          <Button
            type="submit"
            variant="contained"
            size="large"
            fullWidth
            loading={isSubmitting}
          >
            Konto erstellen
          </Button>
          <Typography variant="body2" sx={{ textAlign: 'center' }}>
            Bereits registriert?{' '}
            <Link component={RouterLink} to="/anmelden">
              Anmelden
            </Link>
          </Typography>
        </Stack>
      </form>
    </AuthLayout>
  )
}
