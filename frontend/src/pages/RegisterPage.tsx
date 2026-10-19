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
import { useAuth } from '../auth/useAuth'
import { AuthLayout } from '../components/AuthLayout'
import {
  emailRules,
  emptyToNull,
  newPasswordRules,
  PASSWORD_HINT,
  personNameRules,
  textFieldProps,
} from '../components/formFields'
import { PasswordField } from '../components/PasswordField'

interface RegisterFormValues {
  email: string
  password: string
  firstName: string
  lastName: string
}

export function RegisterPage() {
  const { register: registerAccount } = useAuth()
  const [submitError, setSubmitError] = useState<string>()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFormValues>({
    defaultValues: { email: '', password: '', firstName: '', lastName: '' },
  })

  const onSubmit = async (values: RegisterFormValues) => {
    setSubmitError(undefined)
    try {
      await registerAccount(
        { email: values.email, password: values.password },
        {
          firstName: emptyToNull(values.firstName),
          lastName: emptyToNull(values.lastName),
        },
      )
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
            {...textFieldProps(register('password', newPasswordRules))}
            label="Passwort"
            autoComplete="new-password"
            error={!!errors.password}
            helperText={errors.password?.message ?? PASSWORD_HINT}
          />
          <TextField
            {...textFieldProps(register('firstName', personNameRules))}
            label="Vorname"
            autoComplete="given-name"
            fullWidth
            error={!!errors.firstName}
            helperText={errors.firstName?.message ?? 'Freiwillig'}
          />
          <TextField
            {...textFieldProps(register('lastName', personNameRules))}
            label="Nachname"
            autoComplete="family-name"
            fullWidth
            error={!!errors.lastName}
            helperText={errors.lastName?.message ?? 'Freiwillig'}
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
          <Typography variant="body2" sx={{ textAlign: 'center' }}>
            <Link component={RouterLink} to="/datenschutz">
              Datenschutzerklärung
            </Link>
          </Typography>
        </Stack>
      </form>
    </AuthLayout>
  )
}
