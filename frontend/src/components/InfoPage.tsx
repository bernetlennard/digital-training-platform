import ArrowBack from '@mui/icons-material/ArrowBack'
import { Box, Button, Container, Stack, Typography } from '@mui/material'
import { useEffect, type ReactNode } from 'react'
import { useLocation, useNavigate } from 'react-router'

export function InfoPage({
  title,
  children,
}: {
  title: string
  children: ReactNode
}) {
  const navigate = useNavigate()
  const location = useLocation()

  useEffect(() => {
    window.scrollTo(0, 0)
  }, [])

  // Direkt aufgerufen gibt es keine vorherige Seite, dann geht es zum Start bzw. zur Anmeldung
  const goBack = () => {
    if (location.key === 'default') {
      navigate('/')
    } else {
      navigate(-1)
    }
  }

  return (
    <Box sx={{ minHeight: '100dvh', py: 3 }}>
      <Container maxWidth="md">
        <Stack spacing={3}>
          <Box>
            <Button startIcon={<ArrowBack />} onClick={goBack}>
              Zurück
            </Button>
          </Box>
          <Typography variant="h4" component="h1">
            {title}
          </Typography>
          {children}
        </Stack>
      </Container>
    </Box>
  )
}

export function InfoSection({
  title,
  children,
}: {
  title: string
  children: ReactNode
}) {
  return (
    <Stack spacing={1} component="section">
      <Typography variant="h6" component="h2">
        {title}
      </Typography>
      {children}
    </Stack>
  )
}
