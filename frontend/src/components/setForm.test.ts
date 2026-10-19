import { describe, expect, it } from 'vitest'
import {
  repetitionsRules,
  restSecondsRules,
  toSetFormValues,
  toSetRequest,
  weightRules,
} from './setForm'

describe('UTF-01 Satzwerte prüfen', () => {
  it.each(['0', '82,5', '82.25', '1000', ' 60 '])(
    'Gewicht «%s» ist gültig',
    (value) => {
      expect(weightRules.validate(value)).toBe(true)
    },
  )

  it.each(['', 'abc', '-5', '82,555', '82,', '1e3'])(
    'Gewicht «%s» ist keine Zahl mit höchstens 2 Nachkommastellen',
    (value) => {
      expect(weightRules.validate(value)).toBe(
        'Zahl mit höchstens 2 Nachkommastellen.',
      )
    },
  )

  it.each(['1000,01', '1500'])('Gewicht «%s» liegt über 1000 kg', (value) => {
    expect(weightRules.validate(value)).toBe('0 bis 1000 kg.')
  })

  it.each(['1', '100', ' 10 '])('Wiederholungen «%s» sind gültig', (value) => {
    expect(repetitionsRules.validate(value)).toBe(true)
  })

  it.each(['', '0', '101', '1.5', '-1', 'zehn'])(
    'Wiederholungen «%s» sind ungültig',
    (value) => {
      expect(repetitionsRules.validate(value)).toBe('1 bis 100.')
    },
  )

  it.each(['', '  ', '0', '3600'])(
    'Pause «%s» ist gültig, leer heisst ohne Pause',
    (value) => {
      expect(restSecondsRules.validate(value)).toBe(true)
    },
  )

  it.each(['3601', '1.5', '-1', 'abc'])('Pause «%s» ist ungültig', (value) => {
    expect(restSecondsRules.validate(value)).toBe('0 bis 3600 s.')
  })
})

describe('UTF-02 Satzwerte vorbelegen und umwandeln', () => {
  it('ohne Vorschlag bleibt die Eingabe leer', () => {
    expect(toSetFormValues(undefined)).toEqual({
      weightKg: '',
      repetitions: '',
      restSeconds: '',
    })
  })

  it('ein Vorschlag belegt die Eingabe vor, ohne Pause bleibt das Feld leer', () => {
    const suggestion = {
      setNumber: 2,
      weightKg: 82.5,
      repetitions: 10,
      restSeconds: null,
    }
    expect(toSetFormValues(suggestion)).toEqual({
      weightKg: '82.5',
      repetitions: '10',
      restSeconds: '',
    })
  })

  it('eine Pause von 0 s bleibt erhalten', () => {
    const suggestion = {
      setNumber: 1,
      weightKg: 60,
      repetitions: 12,
      restSeconds: 0,
    }
    expect(toSetFormValues(suggestion).restSeconds).toBe('0')
  })

  it('Komma wird zum Dezimalpunkt, Leerzeichen fallen weg, leere Pause wird null', () => {
    expect(
      toSetRequest({
        weightKg: ' 82,5 ',
        repetitions: ' 10 ',
        restSeconds: '',
      }),
    ).toEqual({ weightKg: 82.5, repetitions: 10, restSeconds: null })
  })

  it('eine angegebene Pause wird als Zahl übernommen', () => {
    expect(
      toSetRequest({ weightKg: '100', repetitions: '8', restSeconds: '90' }),
    ).toEqual({ weightKg: 100, repetitions: 8, restSeconds: 90 })
  })
})
