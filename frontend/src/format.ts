import type { IsoDate, IsoInstant } from './api/types'

// Deutsche Formate (#NFA-7.11): Datum TT.MM.JJJJ, Gewichte in kg
const dateFormat = new Intl.DateTimeFormat('de-CH', {
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
})
const weekdayDateFormat = new Intl.DateTimeFormat('de-CH', {
  weekday: 'short',
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
})
const timeFormat = new Intl.DateTimeFormat('de-CH', {
  hour: '2-digit',
  minute: '2-digit',
})
const numberFormat = new Intl.NumberFormat('de-CH', {
  maximumFractionDigits: 2,
})

/** «2026-10-28» als lokales Datum. Ohne new Date(isoDate), das würde als UTC gelesen. */
function toLocalDate(date: IsoDate) {
  const [year, month, day] = date.split('-').map(Number)
  return new Date(year, month - 1, day)
}

/** «2026-10-28» als «28.10.2026» */
export function formatDate(date: IsoDate) {
  return dateFormat.format(toLocalDate(date))
}

/** «2026-10-28» als «Mi., 28.10.2026», damit man den Trainingstag auf einen Blick erkennt */
export function formatWeekdayDate(date: IsoDate) {
  return weekdayDateFormat.format(toLocalDate(date))
}

/** Datum eines Zeitpunkts in der Zeitzone des Geräts, z. B. «Mi., 28.10.2026» */
export function formatInstantWeekdayDate(instant: IsoInstant) {
  return weekdayDateFormat.format(new Date(instant))
}

/** Start eines Trainings: heute nur die Uhrzeit («um 18:30»), sonst mit Datum («am 28.10.2026, 18:30») */
export function formatStart(instant: IsoInstant) {
  const isToday =
    dateFormat.format(new Date(instant)) === dateFormat.format(new Date())
  return isToday ? `um ${formatTime(instant)}` : `am ${formatDateTime(instant)}`
}

/** Zeitpunkt in der Zeitzone des Geräts, z. B. «28.10.2026, 18:30» */
export function formatDateTime(instant: IsoInstant) {
  const date = new Date(instant)
  return `${dateFormat.format(date)}, ${timeFormat.format(date)}`
}

/** Uhrzeit in der Zeitzone des Geräts, z. B. «18:30» */
export function formatTime(instant: IsoInstant) {
  return timeFormat.format(new Date(instant))
}

/** Dauer zwischen zwei Zeitpunkten, z. B. «1 h 05 min» */
export function formatDuration(start: IsoInstant, end: IsoInstant) {
  const minutes = Math.max(
    0,
    Math.round((Date.parse(end) - Date.parse(start)) / 60_000),
  )
  if (minutes < 60) {
    return `${minutes} min`
  }
  return `${Math.floor(minutes / 60)} h ${String(minutes % 60).padStart(2, '0')} min`
}

export function formatWeight(weightKg: number) {
  return `${numberFormat.format(weightKg)} kg`
}

/** Ein Satz in einer Zeile, z. B. «80 kg × 10 · 90 s Pause» */
export function formatSet(set: {
  weightKg: number
  repetitions: number
  restSeconds: number | null
}) {
  const text = `${formatWeight(set.weightKg)} × ${set.repetitions}`
  return set.restSeconds === null
    ? text
    : `${text} · ${set.restSeconds} s Pause`
}

/** Heutiges Datum im Format JJJJ-MM-TT, in der Zeitzone des Geräts */
export function todayIsoDate(): IsoDate {
  const now = new Date()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${now.getFullYear()}-${month}-${day}`
}

/** Sortiert nach Namen wie im Deutschen üblich, Gross- und Kleinschreibung egal (#FA-2.3) */
export function byName<T extends { name: string }>(a: T, b: T) {
  return a.name.localeCompare(b.name, 'de', { sensitivity: 'base' })
}

/** «1 Übung», «3 Übungen» */
export function countLabel(count: number, singular: string, plural: string) {
  return `${count} ${count === 1 ? singular : plural}`
}
