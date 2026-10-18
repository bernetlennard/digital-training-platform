import ArrowBack from '@mui/icons-material/ArrowBack'
import { IconButton, Stack, Typography } from '@mui/material'
import type { ReactNode } from 'react'
import { Link as RouterLink } from 'react-router'

interface PageHeaderProps {
  title: ReactNode
  /** Zeigt einen Zurück-Knopf zu diesem Pfad */
  backTo?: string
  /** Hauptaktion rechts, z. B. «Neue Übung» */
  action?: ReactNode
}

/** Titel einer Ansicht mit optionalem Zurück-Knopf und Hauptaktion */
export function PageHeader({ title, backTo, action }: PageHeaderProps) {
  return (
    <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
      {backTo && (
        <IconButton
          component={RouterLink}
          to={backTo}
          aria-label="Zurück"
          edge="start"
        >
          <ArrowBack />
        </IconButton>
      )}
      <Typography
        variant="h5"
        component="h1"
        sx={{ flexGrow: 1, minWidth: 0, overflowWrap: 'anywhere' }}
      >
        {title}
      </Typography>
      {action}
    </Stack>
  )
}
