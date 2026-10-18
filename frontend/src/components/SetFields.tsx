import { InputAdornment, Stack, TextField } from '@mui/material'
import type { FieldErrors, UseFormRegister } from 'react-hook-form'
import { textFieldProps } from './formFields'
import {
  repetitionsRules,
  restSecondsRules,
  weightRules,
  type SetFormValues,
} from './setForm'

interface SetFieldsProps {
  register: UseFormRegister<SetFormValues>
  errors: FieldErrors<SetFormValues>
}

function unit(text: string) {
  return {
    endAdornment: <InputAdornment position="end">{text}</InputAdornment>,
  }
}

/**
 * Satz-Eingabe: drei Zahlenfelder nebeneinander. inputMode öffnet auf dem Smartphone die
 * numerische Tastatur (#NFA-5.4), für das Gewicht mit Dezimaltrennzeichen.
 * Die Einheit steht im Feld, damit die Beschriftung auch auf 360 Pixel ganz sichtbar bleibt.
 */
export function SetFields({ register, errors }: SetFieldsProps) {
  return (
    <Stack direction="row" spacing={1}>
      <TextField
        {...textFieldProps(register('weightKg', weightRules))}
        label="Gewicht"
        required
        error={!!errors.weightKg}
        helperText={errors.weightKg?.message}
        autoComplete="off"
        slotProps={{
          input: unit('kg'),
          htmlInput: { inputMode: 'decimal' },
        }}
        sx={{ flex: 1 }}
      />
      <TextField
        {...textFieldProps(register('repetitions', repetitionsRules))}
        label="Wdh."
        required
        error={!!errors.repetitions}
        helperText={errors.repetitions?.message}
        autoComplete="off"
        slotProps={{ htmlInput: { inputMode: 'numeric' } }}
        sx={{ flex: 1 }}
      />
      <TextField
        {...textFieldProps(register('restSeconds', restSecondsRules))}
        label="Pause"
        error={!!errors.restSeconds}
        helperText={errors.restSeconds?.message}
        autoComplete="off"
        slotProps={{
          input: unit('s'),
          htmlInput: { inputMode: 'numeric' },
        }}
        sx={{ flex: 1 }}
      />
    </Stack>
  )
}
