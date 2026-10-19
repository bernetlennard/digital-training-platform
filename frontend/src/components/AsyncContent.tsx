import { Alert, Box, Button, CircularProgress } from '@mui/material'
import type { ReactNode } from 'react'
import { getErrorMessage } from '../api/client'
import type { ApiData } from '../api/useApiData'

interface AsyncContentProps<T> {
  state: ApiData<T>
  errorMessages?: Record<number, string>
  children: (data: T) => ReactNode
}

export function AsyncContent<T>({
  state,
  errorMessages,
  children,
}: AsyncContentProps<T>) {
  if (state.data !== undefined) {
    return children(state.data)
  }
  if (!state.isLoading && state.error !== undefined) {
    return (
      <Alert
        severity="error"
        action={
          <Button color="inherit" onClick={state.reload}>
            Erneut versuchen
          </Button>
        }
      >
        {getErrorMessage(state.error, errorMessages)}
      </Alert>
    )
  }
  return (
    <Box sx={{ display: 'grid', placeItems: 'center', py: 6 }}>
      <CircularProgress aria-label="Wird geladen" />
    </Box>
  )
}
