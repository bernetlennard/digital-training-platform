import { Link, Paper, Typography } from '@mui/material'
import { InfoPage, InfoSection } from '../components/InfoPage'

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

export function PrivacyPage() {
  return (
    <InfoPage title="Datenschutzerklärung">
      <InfoSection title="Verantwortlicher">
        <Typography>
          Verantwortlich für die Bearbeitung der Personendaten ist Lennard
          Bernet als Betreiber der Trainingsplattform. Massgebend ist das
          Bundesgesetz über den Datenschutz (DSG).
        </Typography>
      </InfoSection>

      <InfoSection title="Kontakt">
        <Typography>
          Fragen zum Datenschutz und Anfragen zu deinen Rechten richtest du an:
        </Typography>
        <Typography>
          Lennard Bernet
          <br />
          <Link href="mailto:bernet.lennard.privat@gmail.com">
            bernet.lennard.privat@gmail.com
          </Link>
        </Typography>
      </InfoSection>

      <InfoSection title="Zweck">
        <Typography>
          Die Plattform bearbeitet nur die Daten, die für die Verwaltung deiner
          Trainings nötig sind. Die Daten werden nicht an Dritte weitergegeben.
          Es gibt kein Tracking, keine Analyse-Werkzeuge und keine Werbung.
          Gesundheitsdaten wie Körpergewicht, Körpermasse oder Verletzungen
          werden bewusst nicht erfasst.
        </Typography>
      </InfoSection>

      <InfoSection title="Bearbeitete Daten und Aufbewahrung">
        {/* Blöcke statt Tabelle, damit auf dem Smartphone nichts horizontal scrollt */}
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
      </InfoSection>

      <InfoSection title="Speicherort">
        <Typography>
          Die Plattform läuft auf einem Server von Infomaniak in der Schweiz.
          Infomaniak bearbeitet die Daten als Hosting-Anbieter im Auftrag des
          Verantwortlichen. Es werden keine Daten ins Ausland übermittelt. Die
          Übertragung ist mit HTTPS verschlüsselt.
        </Typography>
      </InfoSection>

      <InfoSection title="Deine Rechte">
        <Typography component="div">
          <ul>
            <li>
              <strong>Auskunft und Herausgabe:</strong> Auf Anfrage erhältst du
              deine Daten als JSON-Datei.
            </li>
            <li>
              <strong>Berichtigung:</strong> Vorname, Nachname, E-Mail-Adresse
              und Passwort änderst du selbst unter «Konto».
            </li>
            <li>
              <strong>Löschung:</strong> Unter «Konto» kannst du dein Konto
              jederzeit löschen. Nach der Bestätigung mit dem Passwort werden
              das Konto und alle Trainingsdaten endgültig gelöscht. Hast du dein
              Passwort vergessen, genügt eine Anfrage per E-Mail an den
              Verantwortlichen.
            </li>
          </ul>
        </Typography>
      </InfoSection>
    </InfoPage>
  )
}
