import FitnessCenter from '@mui/icons-material/FitnessCenter'
import { Box, Container, Paper, Stack, Typography } from '@mui/material'
import type { ReactNode } from 'react'

/** Zentrierter Rahmen für Anmelden und Registrieren, Mobile-First */
export function AuthLayout({
  title,
  children,
}: {
  title: string
  children: ReactNode
}) {
  return (
    <Box
      sx={{ minHeight: '100dvh', display: 'flex', alignItems: 'center', py: 4 }}
    >
      <Container maxWidth="xs">
        <Stack spacing={3} sx={{ alignItems: 'center' }}>
          <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
            <FitnessCenter color="primary" />
            <Typography variant="h6" component="p">
              Trainingsplattform
            </Typography>
          </Stack>
          <Paper variant="outlined" sx={{ p: 3, width: '100%' }}>
            <Typography variant="h5" component="h1" sx={{ mb: 2 }}>
              {title}
            </Typography>
            {children}
          </Paper>
        </Stack>
      </Container>
    </Box>
  )
}
