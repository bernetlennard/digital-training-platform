import { Alert, Snackbar } from '@mui/material'
import { useCallback, useState, type ReactNode } from 'react'
import {
  NotificationContext,
  type NotificationSeverity,
  type Notify,
} from './NotificationContext'

interface Notification {
  /** Neuer key pro Meldung, damit sie eine offene ersetzt und neu einblendet */
  key: number
  message: string
  severity: NotificationSeverity
}

export function NotificationProvider({ children }: { children: ReactNode }) {
  const [notification, setNotification] = useState<Notification>()
  const [open, setOpen] = useState(false)

  const notify = useCallback<Notify>((message, severity = 'success') => {
    setNotification((current) => ({
      key: (current?.key ?? 0) + 1,
      message,
      severity,
    }))
    setOpen(true)
  }, [])

  return (
    <NotificationContext value={notify}>
      {children}
      <Snackbar
        key={notification?.key}
        open={open}
        autoHideDuration={notification?.severity === 'error' ? 6000 : 3000}
        onClose={(_, reason) => {
          if (reason !== 'clickaway') setOpen(false)
        }}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        // --bottom-nav-height setzt AppLayout, ohne Navigation bleibt die Meldung am unteren Rand
        sx={{
          bottom: { xs: 'calc(8px + var(--bottom-nav-height, 0px))', md: 24 },
        }}
      >
        <Alert
          severity={notification?.severity}
          variant="filled"
          onClose={() => setOpen(false)}
          sx={{ width: '100%' }}
        >
          {notification?.message}
        </Alert>
      </Snackbar>
    </NotificationContext>
  )
}
