import Visibility from '@mui/icons-material/Visibility'
import VisibilityOff from '@mui/icons-material/VisibilityOff'
import { IconButton, InputAdornment, TextField } from '@mui/material'
import { useState, type ReactNode } from 'react'
import type { TextFieldRegistration } from './formFields'

interface PasswordFieldProps extends TextFieldRegistration {
  label: string
  autoComplete: 'current-password' | 'new-password'
  error?: boolean
  helperText?: ReactNode
}

/** Passwortfeld mit Anzeigen-Knopf. Einfügen bleibt erlaubt, damit Passwortmanager funktionieren. */
export function PasswordField({
  label,
  autoComplete,
  error,
  helperText,
  ...field
}: PasswordFieldProps) {
  const [visible, setVisible] = useState(false)

  return (
    <TextField
      {...field}
      label={label}
      type={visible ? 'text' : 'password'}
      autoComplete={autoComplete}
      required
      fullWidth
      error={error}
      helperText={helperText}
      slotProps={{
        input: {
          endAdornment: (
            <InputAdornment position="end">
              <IconButton
                aria-label={
                  visible ? 'Passwort verbergen' : 'Passwort anzeigen'
                }
                onClick={() => setVisible((current) => !current)}
                edge="end"
              >
                {visible ? <VisibilityOff /> : <Visibility />}
              </IconButton>
            </InputAdornment>
          ),
        },
      }}
    />
  )
}
