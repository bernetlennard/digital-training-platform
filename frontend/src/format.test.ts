import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import {
  byName,
  countLabel,
  formatDate,
  formatDuration,
  formatSet,
  formatStart,
  formatWeekdayDate,
  todayIsoDate,
} from './format'

// Feste Uhr wie im Backend: 15.10.2026, 10:00 in Zürich (Zeitzone in vite.config.ts)
const NOW = '2026-10-15T08:00:00Z'

describe('UTF-06 Deutsche Formate', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    vi.setSystemTime(NOW)
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('ein Datum erscheint als TT.MM.JJJJ, ohne Verschiebung durch UTC', () => {
    expect(formatDate('2026-10-28')).toBe('28.10.2026')
    expect(formatWeekdayDate('2026-10-20')).toBe('Di., 20.10.2026')
  })

  it('ein Start von heute zeigt nur die Uhrzeit, sonst auch das Datum', () => {
    expect(formatStart('2026-10-15T06:30:00Z')).toBe('um 08:30')
    expect(formatStart('2026-10-14T16:30:00Z')).toBe('am 14.10.2026, 18:30')
  })

  it('heute ist das Datum in der Schweiz, nicht in UTC', () => {
    expect(todayIsoDate()).toBe('2026-10-15')
    // 23:30 UTC ist in Zürich schon der nächste Tag
    vi.setSystemTime('2026-10-15T23:30:00Z')
    expect(todayIsoDate()).toBe('2026-10-16')
  })

  it.each([
    ['2026-10-15T08:00:00Z', '0 min'],
    ['2026-10-15T08:45:00Z', '45 min'],
    ['2026-10-15T09:05:00Z', '1 h 05 min'],
    ['2026-10-15T07:00:00Z', '0 min'],
  ])('Dauer bis %s ergibt «%s»', (end, expected) => {
    expect(formatDuration(NOW, end)).toBe(expected)
  })

  it('ein Satz steht in einer Zeile, die Pause nur wenn erfasst', () => {
    expect(
      formatSet({ weightKg: 82.5, repetitions: 10, restSeconds: 90 }),
    ).toBe('82.5 kg × 10 · 90 s Pause')
    expect(formatSet({ weightKg: 80, repetitions: 8, restSeconds: null })).toBe(
      '80 kg × 8',
    )
  })

  it('Einzahl und Mehrzahl', () => {
    expect(countLabel(1, 'Übung', 'Übungen')).toBe('1 Übung')
    expect(countLabel(0, 'Übung', 'Übungen')).toBe('0 Übungen')
    expect(countLabel(3, 'Übung', 'Übungen')).toBe('3 Übungen')
  })

  it('Namen werden alphabetisch sortiert, ohne Gross- und Kleinschreibung', () => {
    const names = ['Zug', 'Überzüge', 'bankdrücken', 'Ausfallschritt']
      .map((name) => ({ name }))
      .toSorted(byName)
      .map(({ name }) => name)
    expect(names).toEqual(['Ausfallschritt', 'bankdrücken', 'Überzüge', 'Zug'])
  })
})
