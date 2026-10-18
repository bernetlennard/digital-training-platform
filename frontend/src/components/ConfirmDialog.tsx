import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Stack,
} from '@mui/material'
import { useState, type ReactNode } from 'react'
import { getErrorMessage } from '../api/client'

interface ConfirmDialogProps {
  title: string
  children: ReactNode
  confirmLabel: string
  cancelLabel?: string
  /** Rot für Aktionen, die Daten verwerfen */
  destructive?: boolean
  /** Eigene Fehlermeldungen je Statuscode */
  errorMessages?: Record<number, string>
  /** Führt die Aktion aus. Bei Erfolg schliesst der Aufrufer den Dialog. */
  onConfirm: () => Promise<void>
  onClose: () => void
}

/**
 * Rückfrage vor dem Löschen oder Abbrechen (#NFA-7.5). Schlägt die Aktion fehl, bleibt der Dialog
 * offen und zeigt die Meldung (#NFA-7.4).
 */
export function ConfirmDialog({
  title,
  children,
  confirmLabel,
  cancelLabel = 'Abbrechen',
  destructive = false,
  errorMessages,
  onConfirm,
  onClose,
}: ConfirmDialogProps) {
  const [isPending, setIsPending] = useState(false)
  const [error, setError] = useState<string>()

  const handleConfirm = async () => {
    setError(undefined)
    setIsPending(true)
    try {
      await onConfirm()
    } catch (caught) {
      setError(getErrorMessage(caught, errorMessages))
      setIsPending(false)
    }
  }

  return (
    <Dialog
      open
      onClose={isPending ? undefined : onClose}
      fullWidth
      maxWidth="xs"
    >
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <Stack spacing={2}>
          {error && <Alert severity="error">{error}</Alert>}
          <DialogContentText component="div">{children}</DialogContentText>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>
          {cancelLabel}
        </Button>
        <Button
          variant="contained"
          color={destructive ? 'error' : 'primary'}
          loading={isPending}
          onClick={handleConfirm}
        >
          {confirmLabel}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
