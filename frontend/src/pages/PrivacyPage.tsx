import ArrowBack from '@mui/icons-material/ArrowBack'
import { Box, Button, Container, Paper, Stack, Typography } from '@mui/material'
import type { ReactNode } from 'react'
import { useLocation, useNavigate } from 'react-router'

// Inhalt gemäss Kapitel «Datenschutz und rechtliche Aspekte»
const DATA_CATEGORIES = [
  {
    category: 'Konto',
    data: 'E-Mail-Adresse, Passwort (nur als Hash), freiwillig Vor- und Nachname',
    purpose: 'Anmeldung, Kontoverwaltung',
    retention: 'Bis zur Löschung des Kontos',
  },
  {
    category: 'Trainingsdaten',
    data: 'Übungen, Routinen, geplante Trainings, Trainingseinheiten, Sätze mit Gewicht, Wiederholungen und Pause',
    purpose: 'Trainingsverwaltung',
    retention: 'Bis zur Löschung des Kontos',
  },
  {
    category: 'Technische Daten',
    data: 'IP-Adresse, Zeitstempel, Sicherheitsereignisse',
    purpose: 'Betrieb und Sicherheit',
    retention: 'Höchstens 14 Tage in den Logdateien',
  },
  {
    category: 'Cookie',
    data: 'Anmelde-Token (HttpOnly)',
    purpose: 'Anmeldung',
    retention: 'Höchstens 8 Stunden',
  },
]

function Section({ title, children }: { title: string; children: ReactNode }) {
  return (
    <Stack spacing={1} component="section">
      <Typography variant="h6" component="h2">
        {title}
      </Typography>
      {children}
    </Stack>
  )
}

/** Datenschutzerklärung, auch ohne Anmeldung erreichbar (#FA-1.4) */
export function PrivacyPage() {
  const navigate = useNavigate()
  const location = useLocation()

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
            Datenschutzerklärung
          </Typography>

          <Section title="Verantwortlicher">
            <Typography>
              Verantwortlich für die Bearbeitung der Personendaten ist Lennard
              Bernet als Betreiber der Trainingsplattform. Massgebend ist das
              Bundesgesetz über den Datenschutz (DSG).
            </Typography>
          </Section>

          <Section title="Zweck">
            <Typography>
              Die Plattform bearbeitet nur die Daten, die für die Verwaltung
              deiner Trainings nötig sind. Die Daten werden nicht an Dritte
              weitergegeben. Es gibt kein Tracking, keine Analyse-Werkzeuge und
              keine Werbung. Gesundheitsdaten wie Körpergewicht, Körpermasse
              oder Verletzungen werden bewusst nicht erfasst.
            </Typography>
          </Section>

          <Section title="Bearbeitete Daten und Aufbewahrung">
            {/* Als Blöcke statt Tabelle, damit auf 360 Pixel nichts horizontal scrollt (#NFA-7.1) */}
            {DATA_CATEGORIES.map((row) => (
              <Paper key={row.category} variant="outlined" sx={{ p: 2 }}>
                <Typography variant="subtitle1" component="h3">
                  {row.category}
                </Typography>
                <Typography variant="body2">
                  <strong>Daten:</strong> {row.data}
                </Typography>
                <Typography variant="body2">
                  <strong>Zweck:</strong> {row.purpose}
                </Typography>
                <Typography variant="body2">
                  <strong>Aufbewahrung:</strong> {row.retention}
                </Typography>
              </Paper>
            ))}
          </Section>

          <Section title="Speicherort">
            <Typography>
              Die Plattform läuft auf einem Server von Infomaniak in der
              Schweiz. Infomaniak bearbeitet die Daten als Hosting-Anbieter im
              Auftrag des Verantwortlichen. Es werden keine Daten ins Ausland
              übermittelt. Die Übertragung ist mit HTTPS verschlüsselt.
            </Typography>
          </Section>

          <Section title="Deine Rechte">
            <Typography component="div">
              <ul>
                <li>
                  <strong>Auskunft und Herausgabe:</strong> Auf Anfrage erhältst
                  du deine Daten als JSON-Datei.
                </li>
                <li>
                  <strong>Berichtigung:</strong> Vorname, Nachname,
                  E-Mail-Adresse und Passwort änderst du selbst unter «Konto».
                </li>
                <li>
                  <strong>Löschung:</strong> Unter «Konto» kannst du dein Konto
                  jederzeit löschen. Nach der Bestätigung mit dem Passwort
                  werden das Konto und alle Trainingsdaten endgültig gelöscht.
                  Hast du dein Passwort vergessen, genügt eine Anfrage per
                  E-Mail an den Verantwortlichen.
                </li>
              </ul>
            </Typography>
          </Section>
        </Stack>
      </Container>
    </Box>
  )
}
