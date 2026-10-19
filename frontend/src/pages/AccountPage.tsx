import Logout from '@mui/icons-material/Logout'
import {
  Alert,
  Button,
  Link,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { useState, type ReactNode } from 'react'
import { useForm } from 'react-hook-form'
import { Link as RouterLink } from 'react-router'
import { getErrorMessage } from '../api/client'
import type { UserResponse } from '../api/types'
import { updateProfile } from '../api/users'
import { useAuth } from '../auth/useAuth'
import {
  ChangeEmailDialog,
  ChangePasswordDialog,
  DeleteAccountDialog,
} from '../components/AccountDialogs'
import {
  emptyToNull,
  personNameRules,
  textFieldProps,
} from '../components/formFields'
import { PageHeader } from '../components/PageHeader'
import { useNotify } from '../notifications/useNotify'

type DialogType = 'email' | 'password' | 'delete'

export function AccountPage() {
  const { user, logout } = useAuth()
  const [dialog, setDialog] = useState<DialogType>()
  const [isLoggingOut, setIsLoggingOut] = useState(false)
  const [logoutError, setLogoutError] = useState<string>()

  const handleLogout = async () => {
    setLogoutError(undefined)
    setIsLoggingOut(true)
    try {
      await logout()
    } catch (error) {
      setLogoutError(getErrorMessage(error))
      setIsLoggingOut(false)
    }
  }

  // RequireAuth zeigt die Ansicht nur mit Anmeldung
  if (!user) {
    return null
  }

  return (
    <>
      <PageHeader title="Konto" />
      <Stack spacing={3}>
        <Section title="Profil">
          <ProfileForm user={user} />
        </Section>

        <Section title="Anmeldung">
          <Typography sx={{ overflowWrap: 'anywhere' }}>
            Angemeldet als <strong>{user.email}</strong>
          </Typography>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
            <Button variant="outlined" onClick={() => setDialog('email')}>
              E-Mail-Adresse ändern
            </Button>
            <Button variant="outlined" onClick={() => setDialog('password')}>
              Passwort ändern
            </Button>
          </Stack>
          {logoutError && <Alert severity="error">{logoutError}</Alert>}
          <Button
            variant="contained"
            startIcon={<Logout />}
            loading={isLoggingOut}
            onClick={handleLogout}
            sx={{ alignSelf: 'flex-start' }}
          >
            Abmelden
          </Button>
        </Section>

        <Section title="Konto löschen">
          <Typography color="text.secondary">
            Löscht dein Konto mit allen Übungen, Routinen und Trainings
            endgültig.
          </Typography>
          <Button
            variant="outlined"
            color="error"
            onClick={() => setDialog('delete')}
            sx={{ alignSelf: 'flex-start' }}
          >
            Konto löschen
          </Button>
        </Section>

        <Typography variant="body2">
          <Link component={RouterLink} to="/datenschutz">
            Datenschutzerklärung
          </Link>
        </Typography>
      </Stack>

      {dialog === 'email' && (
        <ChangeEmailDialog onClose={() => setDialog(undefined)} />
      )}
      {dialog === 'password' && (
        <ChangePasswordDialog onClose={() => setDialog(undefined)} />
      )}
      {dialog === 'delete' && (
        <DeleteAccountDialog onClose={() => setDialog(undefined)} />
      )}
    </>
  )
}

function Section({ title, children }: { title: string; children: ReactNode }) {
  return (
    <Paper variant="outlined" component="section" sx={{ p: 2 }}>
      <Stack spacing={2}>
        <Typography variant="h6" component="h2">
          {title}
        </Typography>
        {children}
      </Stack>
    </Paper>
  )
}

interface ProfileFormValues {
  firstName: string
  lastName: string
}

function ProfileForm({ user }: { user: UserResponse }) {
  const { setUser } = useAuth()
  const notify = useNotify()
  const [submitError, setSubmitError] = useState<string>()
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting, isDirty },
  } = useForm<ProfileFormValues>({
    defaultValues: {
      firstName: user.firstName ?? '',
      lastName: user.lastName ?? '',
    },
  })

  const onSubmit = async (values: ProfileFormValues) => {
    setSubmitError(undefined)
    try {
      const updated = await updateProfile({
        firstName: emptyToNull(values.firstName),
        lastName: emptyToNull(values.lastName),
      })
      setUser(updated)
      reset({
        firstName: updated.firstName ?? '',
        lastName: updated.lastName ?? '',
      })
      notify('Profil gespeichert')
    } catch (error) {
      setSubmitError(getErrorMessage(error))
    }
  }

  return (
    <form noValidate onSubmit={handleSubmit(onSubmit)}>
      <Stack spacing={2}>
        {submitError && <Alert severity="error">{submitError}</Alert>}
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
          <TextField
            {...textFieldProps(register('firstName', personNameRules))}
            label="Vorname"
            autoComplete="given-name"
            fullWidth
            error={!!errors.firstName}
            helperText={errors.firstName?.message}
          />
          <TextField
            {...textFieldProps(register('lastName', personNameRules))}
            label="Nachname"
            autoComplete="family-name"
            fullWidth
            error={!!errors.lastName}
            helperText={errors.lastName?.message}
          />
        </Stack>
        <Button
          type="submit"
          variant="contained"
          loading={isSubmitting}
          disabled={!isDirty}
          sx={{ alignSelf: 'flex-start' }}
        >
          Speichern
        </Button>
      </Stack>
    </form>
  )
}
