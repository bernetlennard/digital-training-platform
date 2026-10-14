import { Box, CircularProgress } from '@mui/material'

export function LoadingScreen() {
  return (
    <Box sx={{ minHeight: '100dvh', display: 'grid', placeItems: 'center' }}>
      <CircularProgress aria-label="Wird geladen" />
    </Box>
  )
}
