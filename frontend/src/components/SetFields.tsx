import { InputAdornment, Stack, TextField } from '@mui/material'
import type { FocusEvent } from 'react'
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

// Markiert den vorbelegten Wert, damit eine neue Zahl ihn gleich ersetzt
function selectAll(event: FocusEvent<HTMLInputElement>) {
  event.currentTarget.select()
}

// Die Beschriftung steht immer oben (shrink), sonst überdeckt sie im leeren Feld die Einheit
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
          inputLabel: { shrink: true },
          htmlInput: { inputMode: 'decimal', onFocus: selectAll },
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
        slotProps={{
          inputLabel: { shrink: true },
          htmlInput: { inputMode: 'numeric', onFocus: selectAll },
        }}
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
          inputLabel: { shrink: true },
          htmlInput: { inputMode: 'numeric', onFocus: selectAll },
        }}
        sx={{ flex: 1 }}
      />
    </Stack>
  )
}
