import type { RegisterOptions, UseFormRegisterReturn } from 'react-hook-form'

// Die ref gehört ans Eingabefeld (inputRef), nicht an den Rahmen von TextField
export function textFieldProps({ ref, ...field }: UseFormRegisterReturn) {
  return { ...field, inputRef: ref }
}

export type TextFieldRegistration = ReturnType<typeof textFieldProps>

// Die Regeln dienen nur der Benutzerführung, verbindlich prüft das Backend

export const emailRules = {
  required: 'Bitte E-Mail-Adresse eingeben.',
  maxLength: { value: 254, message: 'Höchstens 254 Zeichen.' },
  pattern: {
    value: /^[^\s@]+@[^\s@]+$/,
    message: 'Bitte gültige E-Mail-Adresse eingeben.',
  },
} satisfies RegisterOptions

export const PASSWORD_HINT = '15 bis 64 Zeichen'

export const newPasswordRules = {
  required: 'Bitte Passwort eingeben.',
  minLength: { value: 15, message: `Das Passwort braucht ${PASSWORD_HINT}.` },
  maxLength: { value: 64, message: `Das Passwort braucht ${PASSWORD_HINT}.` },
} satisfies RegisterOptions

export const currentPasswordRules = {
  required: 'Bitte Passwort eingeben.',
} satisfies RegisterOptions

export const nameRules = {
  validate: (value: string) => value.trim() !== '' || 'Bitte Namen eingeben.',
  maxLength: { value: 100, message: 'Höchstens 100 Zeichen.' },
} satisfies RegisterOptions

export const personNameRules = {
  maxLength: { value: 100, message: 'Höchstens 100 Zeichen.' },
} satisfies RegisterOptions

export const longTextRules = {
  maxLength: { value: 1000, message: 'Höchstens 1000 Zeichen.' },
} satisfies RegisterOptions

export function emptyToNull(value: string) {
  const trimmed = value.trim()
  return trimmed === '' ? null : trimmed
}
