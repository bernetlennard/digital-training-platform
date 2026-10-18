import { Alert, TextField } from '@mui/material'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { getErrorMessage } from '../api/client'
import { changeEmail, changePassword, deleteAccount } from '../api/users'
import { useAuth } from '../auth/useAuth'
import { useNotify } from '../notifications/useNotify'
import { FormDialog } from './FormDialog'
import {
  currentPasswordRules,
  emailRules,
  newPasswordRules,
  PASSWORD_HINT,
  textFieldProps,
} from './formFields'
import { PasswordField } from './PasswordField'

const WRONG_PASSWORD = 'Das Passwort ist falsch.'

interface DialogProps {
  onClose: () => void
}

interface ChangeEmailFormValues {
  newEmail: string
  password: string
}

/** Neue E-Mail-Adresse, bestätigt mit dem Passwort (#FA-1.8) */
export function ChangeEmailDialog({ onClose }: DialogProps) {
  const { setUser } = useAuth()
  const notify = useNotify()
  const [submitError, setSubmitError] = useState<string>()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ChangeEmailFormValues>({
    defaultValues: { newEmail: '', password: '' },
  })

  const onSubmit = async (values: ChangeEmailFormValues) => {
    setSubmitError(undefined)
    try {
      setUser(await changeEmail(values))
      notify('E-Mail-Adresse geändert')
      onClose()
    } catch (error) {
      setSubmitError(
        getErrorMessage(error, {
          401: WRONG_PASSWORD,
          409: 'Für diese E-Mail-Adresse besteht bereits ein Konto.',
        }),
      )
    }
  }

  return (
    <FormDialog
      title="E-Mail-Adresse ändern"
      isSubmitting={isSubmitting}
      error={submitError}
      onSubmit={handleSubmit(onSubmit)}
      onClose={onClose}
    >
      <TextField
        {...textFieldProps(register('newEmail', emailRules))}
        label="Neue E-Mail-Adresse"
        type="email"
        autoComplete="email"
        required
        fullWidth
        error={!!errors.newEmail}
        helperText={errors.newEmail?.message}
      />
      <PasswordField
        {...textFieldProps(register('password', currentPasswordRules))}
        label="Passwort zur Bestätigung"
        autoComplete="current-password"
        error={!!errors.password}
        helperText={errors.password?.message}
      />
    </FormDialog>
  )
}

interface ChangePasswordFormValues {
  currentPassword: string
  newPassword: string
}

/** Neues Passwort, bestätigt mit dem aktuellen (#FA-1.9) */
export function ChangePasswordDialog({ onClose }: DialogProps) {
  const notify = useNotify()
  const [submitError, setSubmitError] = useState<string>()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ChangePasswordFormValues>({
    defaultValues: { currentPassword: '', newPassword: '' },
  })

  const onSubmit = async (values: ChangePasswordFormValues) => {
    setSubmitError(undefined)
    try {
      await changePassword(values)
      notify('Passwort geändert')
      onClose()
    } catch (error) {
      setSubmitError(
        getErrorMessage(error, {
          401: 'Das aktuelle Passwort ist falsch.',
        }),
      )
    }
  }

  return (
    <FormDialog
      title="Passwort ändern"
      isSubmitting={isSubmitting}
      error={submitError}
      onSubmit={handleSubmit(onSubmit)}
      onClose={onClose}
    >
      <PasswordField
        {...textFieldProps(register('currentPassword', currentPasswordRules))}
        label="Aktuelles Passwort"
        autoComplete="current-password"
        error={!!errors.currentPassword}
        helperText={errors.currentPassword?.message}
      />
      <PasswordField
        {...textFieldProps(register('newPassword', newPasswordRules))}
        label="Neues Passwort"
        autoComplete="new-password"
        error={!!errors.newPassword}
        helperText={errors.newPassword?.message ?? PASSWORD_HINT}
      />
    </FormDialog>
  )
}

interface DeleteAccountFormValues {
  password: string
}

/** Konto mit allen Daten endgültig löschen, bestätigt mit dem Passwort (SAF4, #FA-1.10) */
export function DeleteAccountDialog({ onClose }: DialogProps) {
  const { setUser } = useAuth()
  const notify = useNotify()
  const [submitError, setSubmitError] = useState<string>()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<DeleteAccountFormValues>({ defaultValues: { password: '' } })

  const onSubmit = async (values: DeleteAccountFormValues) => {
    setSubmitError(undefined)
    try {
      await deleteAccount(values)
      notify('Dein Konto wurde gelöscht')
      // Danach leitet RequireAuth zur Anmeldung weiter
      setUser(null)
    } catch (error) {
      setSubmitError(getErrorMessage(error, { 401: WRONG_PASSWORD }))
    }
  }

  return (
    <FormDialog
      title="Konto löschen"
      submitLabel="Endgültig löschen"
      destructive
      isSubmitting={isSubmitting}
      error={submitError}
      onSubmit={handleSubmit(onSubmit)}
      onClose={onClose}
    >
      <Alert severity="warning">
        Dein Konto und alle Trainingsdaten werden endgültig gelöscht. Das lässt
        sich nicht rückgängig machen.
      </Alert>
      <PasswordField
        {...textFieldProps(register('password', currentPasswordRules))}
        label="Passwort zur Bestätigung"
        autoComplete="current-password"
        error={!!errors.password}
        helperText={errors.password?.message}
      />
    </FormDialog>
  )
}
