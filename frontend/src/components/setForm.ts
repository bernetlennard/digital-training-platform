import type { RegisterOptions } from 'react-hook-form'
import type { SetRequest, SetSuggestion } from '../api/types'

/** Eingabe eines Satzes. Textfelder, damit auch «82,5» mit Komma geht. */
export interface SetFormValues {
  weightKg: string
  repetitions: string
  restSeconds: string
}

/** Komma oder Punkt als Dezimaltrennzeichen */
function parseDecimal(value: string) {
  return Number(value.trim().replace(',', '.'))
}

const WHOLE_NUMBER = /^\d+$/

// Plausible Satzwerte (#FA-5.3), verbindlich prüft das Backend
export const weightRules = {
  required: 'Gewicht fehlt.',
  validate: (value: string) => {
    if (!/^\d+([.,]\d{1,2})?$/.test(value.trim())) {
      return 'Zahl mit höchstens 2 Nachkommastellen.'
    }
    return parseDecimal(value) <= 1000 || '0 bis 1000 kg.'
  },
} satisfies RegisterOptions<SetFormValues, 'weightKg'>

export const repetitionsRules = {
  required: 'Wdh. fehlen.',
  validate: (value: string) => {
    const repetitions = Number(value.trim())
    return (
      (WHOLE_NUMBER.test(value.trim()) &&
        repetitions >= 1 &&
        repetitions <= 100) ||
      '1 bis 100.'
    )
  },
} satisfies RegisterOptions<SetFormValues, 'repetitions'>

/** Die Pause ist freiwillig */
export const restSecondsRules = {
  validate: (value: string) =>
    value.trim() === '' ||
    (WHOLE_NUMBER.test(value.trim()) && Number(value.trim()) <= 3600) ||
    '0 bis 3600 s.',
} satisfies RegisterOptions<SetFormValues, 'restSeconds'>

/** Vorbelegung aus einem Vorschlag oder einem bestehenden Satz, sonst leer (#FA-5.5) */
export function toSetFormValues(set: SetSuggestion | undefined): SetFormValues {
  return {
    weightKg: set ? String(set.weightKg) : '',
    repetitions: set ? String(set.repetitions) : '',
    restSeconds: set?.restSeconds == null ? '' : String(set.restSeconds),
  }
}

export function toSetRequest(values: SetFormValues): SetRequest {
  return {
    weightKg: parseDecimal(values.weightKg),
    repetitions: Number(values.repetitions.trim()),
    restSeconds:
      values.restSeconds.trim() === ''
        ? null
        : Number(values.restSeconds.trim()),
  }
}
