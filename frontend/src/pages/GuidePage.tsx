import { Typography } from '@mui/material'
import { InfoPage, InfoSection } from '../components/InfoPage'

// Derselbe Text steht in frontend/Benutzeranleitung.docx für die Diplomarbeit
const SECTIONS = [
  {
    title: 'Überblick',
    paragraphs: [
      'Mit der Trainingsplattform verwaltest du deine Übungen und Routinen, planst deine Trainings und protokollierst sie direkt im Training. Zuerst erfasst du deine Übungen und stellst daraus eine Routine zusammen. Diese Routine kannst du danach einplanen oder spontan starten. Im Training erfasst du deine Sätze und nach dem Abschliessen findest du alles im Verlauf.',
      'Die Plattform ist für das Smartphone gemacht. Am unteren Rand ist die Navigation mit Start, Routinen, Übungen und Verlauf. Auf einem Tablet oder Computer ist sie als Seitenleiste links. Oben rechts kommst du zu dieser Anleitung und zu deinem Konto.',
    ],
  },
  {
    title: 'Registrieren und anmelden',
    paragraphs: [
      'Für die Plattform brauchst du ein Konto. Unter «Registrieren» gibst du deine E-Mail-Adresse und ein Passwort ein. Vor- und Nachname sind freiwillig. Nach «Konto erstellen» bist du direkt angemeldet.',
      'Danach meldest du dich unter «Anmelden» mit E-Mail-Adresse und Passwort an. Die Anmeldung gilt für acht Stunden, danach musst du dich neu anmelden. Nach mehreren falschen Passwörtern wird die Anmeldung zum Schutz für 15 Minuten gesperrt.',
    ],
  },
  {
    title: 'Übungen erfassen',
    paragraphs: [
      'Unter «Übungen» baust du deinen eigenen Übungskatalog auf. Mit «Neue Übung» gibst du den Namen ein und wählst die Muskelgruppe. In der Beschreibung kannst du Hinweise zur Ausführung notieren, zum Beispiel die Einstellung am Gerät. Diese Beschreibung siehst du später auch im Training.',
      'Tippst du eine Übung an, kannst du sie ändern oder löschen. Kommt die Übung noch in einer Routine vor, musst du sie zuerst dort entfernen. Abgeschlossene Trainings mit dieser Übung bleiben im Verlauf vollständig erhalten.',
    ],
  },
  {
    title: 'Routinen zusammenstellen',
    paragraphs: [
      'Eine Routine ist dein Trainingsprogramm mit mehreren Übungen, zum Beispiel für den Oberkörper. Unter «Routinen» erstellst du mit «Neue Routine» eine neue Routine. Gib einen Namen ein und wähle über «Übungen hinzufügen» die Übungen aus. Sie kommen in der Reihenfolge in die Routine, in der du sie antippst. Mit den Pfeilen änderst du die Reihenfolge und mit dem Kreuz entfernst du eine Übung wieder. Nach «Speichern» ist die Routine bereit.',
      'Löschst du eine Routine, werden auch ihre geplanten Trainings gelöscht. Abgeschlossene Trainings bleiben im Verlauf.',
    ],
  },
  {
    title: 'Training planen',
    paragraphs: [
      'Auf der Startseite planst du mit «Einplanen» ein Training. Dafür wählst du die Routine und das Datum, frühestens heute. Die geplanten Trainings stehen nach Datum sortiert auf der Startseite und das nächste ist markiert. Mit dem Stift änderst du ein geplantes Training und mit dem Papierkorb löschst du es.',
      'Ein verpasstes Training wird als «vergangen» angezeigt. Du kannst es noch starten oder löschen, aber nicht mehr verschieben.',
    ],
  },
  {
    title: 'Training durchführen',
    paragraphs: [
      'Ein geplantes Training startest du auf der Startseite mit «Starten». Ohne Planung tippst du auf «Spontan starten» und wählst die Routine aus. Es kann immer nur ein Training gleichzeitig laufen.',
      'Oben siehst du die Startzeit und wie lange das Training schon läuft. Jede Übung hat einen eigenen Bereich, den du mit einem Tipp auf- und zuklappst. Für einen Satz gibst du das Gewicht in kg, die Wiederholungen und freiwillig die Pause in Sekunden ein. Mit «Satz 1 speichern» ist der erste Satz erfasst.',
      'Hast du die Routine schon einmal trainiert, sind die Felder mit den Werten vom letzten Training vorbelegt. Die weiteren Sätze stehen als «offen» darunter. Somit musst du die Werte nur noch anpassen oder direkt speichern. Sind keine Sätze mehr offen, erscheint bei der Übung ein grünes Häkchen.',
      'Tippst du einen gespeicherten Satz an, kannst du die Werte ändern, den Satz an eine andere Position verschieben oder ihn löschen. Unten gibt es ein Feld für eine Notiz zum Training. Sie wird gespeichert, sobald du das Feld verlässt.',
      'Mit dem Pfeil oben links kommst du zur Startseite, das Training läuft dabei weiter. Mit «Fortsetzen» bist du wieder im Training. Auch wenn du die Seite schliesst, geht nichts verloren.',
      'Am Schluss tippst du auf «Abschliessen». Dafür braucht es mindestens einen Satz. Übungen ohne Satz werden nicht gespeichert und das Training lässt sich danach nicht mehr ändern. Mit «Abbrechen» werden alle Sätze verworfen. Ein geplantes Training bleibt dabei eingeplant.',
    ],
  },
  {
    title: 'Verlauf ansehen',
    paragraphs: [
      'Unter «Verlauf» findest du deine abgeschlossenen Trainings nach Routine geordnet. Tippst du eine Routine an, siehst du ihre Trainings mit Datum und Dauer, das neuste zuoberst. Im Training selbst stehen alle Übungen mit ihren Sätzen und der Notiz. So siehst du, wie du dich von Mal zu Mal steigerst.',
      'Mit «Training löschen» entfernst du ein Training aus dem Verlauf. Gelöschte Routinen bleiben im Verlauf und sind als «gelöscht» markiert.',
    ],
  },
  {
    title: 'Konto verwalten',
    paragraphs: [
      'Über das Symbol mit der Person oben rechts kommst du zu deinem Konto. Dort änderst du deinen Namen, deine E-Mail-Adresse und dein Passwort. Bei der E-Mail-Adresse und beim Passwort musst du zur Sicherheit dein aktuelles Passwort eingeben. Mit «Abmelden» meldest du dich ab, was vor allem auf einem fremden Gerät wichtig ist.',
      'Mit «Konto löschen» werden dein Konto und alle Trainingsdaten nach der Bestätigung mit dem Passwort endgültig gelöscht. Welche Daten die Plattform speichert, steht in der Datenschutzerklärung.',
    ],
  },
]

export function GuidePage() {
  return (
    <InfoPage title="Anleitung">
      {SECTIONS.map((section) => (
        <InfoSection key={section.title} title={section.title}>
          {section.paragraphs.map((paragraph) => (
            <Typography key={paragraph}>{paragraph}</Typography>
          ))}
        </InfoSection>
      ))}
    </InfoPage>
  )
}
