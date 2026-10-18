import { Paper, Stack, Typography } from '@mui/material'
import type { ReactNode } from 'react'

interface EmptyStateProps {
  title: string
  /** Hinweis auf den nächsten Schritt */
  text?: string
  action?: ReactNode
}

/** Leerer Zustand einer Liste mit Hinweis, wie es weitergeht */
export function EmptyState({ title, text, action }: EmptyStateProps) {
  return (
    <Paper variant="outlined" sx={{ p: 3 }}>
      <Stack spacing={1} sx={{ alignItems: 'center', textAlign: 'center' }}>
        <Typography variant="subtitle1" component="p">
          {title}
        </Typography>
        {text && <Typography color="text.secondary">{text}</Typography>}
        {action}
      </Stack>
    </Paper>
  )
}
