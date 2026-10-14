import type { RegisterOptions, UseFormRegisterReturn } from 'react-hook-form'

/**
 * Verbindet ein Feld von React Hook Form mit einem MUI-TextField.
 * Die ref muss ans Eingabefeld selbst (inputRef), nicht an den Rahmen von TextField.
 */
export function textFieldProps({ ref, ...field }: UseFormRegisterReturn) {
  return { ...field, inputRef: ref }
}

export type TextFieldRegistration = ReturnType<typeof textFieldProps>

// Nur für die Benutzerführung, verbindlich prüft das Backend (#NFA-7.9)
export const emailRules = {
  required: 'Bitte E-Mail-Adresse eingeben.',
  pattern: {
    value: /^[^\s@]+@[^\s@]+$/,
    message: 'Bitte gültige E-Mail-Adresse eingeben.',
  },
} satisfies RegisterOptions
