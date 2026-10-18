import {
  Alert,
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
} from '@mui/material'
import type { FormEventHandler, ReactNode } from 'react'

interface FormDialogProps {
  title: string
  submitLabel?: string
  /** Rot für «Konto löschen» */
  destructive?: boolean
  isSubmitting: boolean
  /** Meldung, wenn das Speichern fehlschlägt. Die Eingaben bleiben erhalten (#NFA-5.2). */
  error?: string
  onSubmit: FormEventHandler<HTMLFormElement>
  onClose: () => void
  /** Zusätzliche Aktion links, z. B. «Löschen» */
  secondaryAction?: ReactNode
  children: ReactNode
}

/** Dialog für kurze Formulare. Der Aufrufer zeigt ihn nur an, solange er offen ist. */
export function FormDialog({
  title,
  submitLabel = 'Speichern',
  destructive = false,
  isSubmitting,
  error,
  onSubmit,
  onClose,
  secondaryAction,
  children,
}: FormDialogProps) {
  return (
    <Dialog
      open
      onClose={isSubmitting ? undefined : onClose}
      fullWidth
      maxWidth="sm"
    >
      <form noValidate onSubmit={onSubmit}>
        <DialogTitle>{title}</DialogTitle>
        <DialogContent>
          {/* Abstand oben, damit die Beschriftung des ersten Feldes nicht abgeschnitten wird */}
          <Stack spacing={2} sx={{ pt: 1 }}>
            {error && <Alert severity="error">{error}</Alert>}
            {children}
          </Stack>
        </DialogContent>
        <DialogActions>
          {secondaryAction}
          <Box sx={{ flexGrow: 1 }} />
          <Button onClick={onClose} disabled={isSubmitting}>
            Abbrechen
          </Button>
          <Button
            type="submit"
            variant="contained"
            color={destructive ? 'error' : 'primary'}
            loading={isSubmitting}
          >
            {submitLabel}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}
