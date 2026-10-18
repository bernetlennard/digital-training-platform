import { createContext } from 'react'

export type NotificationSeverity = 'success' | 'error' | 'info'

/** Zeigt eine kurze Rückmeldung als Snackbar unten rechts (#NFA-7.4) */
export type Notify = (message: string, severity?: NotificationSeverity) => void

export const NotificationContext = createContext<Notify | null>(null)
