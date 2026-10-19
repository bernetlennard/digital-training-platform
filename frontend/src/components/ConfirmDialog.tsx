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
  destructive?: boolean
  errorMessages?: Record<number, string>
  /** Bei Erfolg schliesst der Aufrufer den Dialog, bei einem Fehler bleibt er mit der Meldung offen */
  onConfirm: () => Promise<void>
  onClose: () => void
}

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
