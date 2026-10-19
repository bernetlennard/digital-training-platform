import { Paper, Stack, Typography } from '@mui/material'
import type { ReactNode } from 'react'

interface EmptyStateProps {
  title: string
  text?: string
  action?: ReactNode
}

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
