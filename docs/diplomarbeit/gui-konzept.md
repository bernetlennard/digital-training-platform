# GUI-Konzept

Das Frontend ist eine React-SPA mit Material UI im Mobile-First-Layout (E3, E6). Es zeigt Daten an und nimmt Eingaben entgegen, die fachlichen Regeln prüft das Backend (L5, L6). Jede Ansicht gehört zu einem Systemanwendungsfall und spricht nur die Endpunkte der REST-Schnittstelle an.

## Navigation

- **Öffentlich:** Anmelden, Registrieren und Datenschutz. Alle anderen Ansichten verlangen eine Anmeldung (#NFA-7.7).
- **Hauptnavigation:** Eine Leiste am unteren Rand mit den vier Bereichen Start, Routinen, Übungen und Verlauf. Ab Tablet-Breite wird daraus eine Seitenleiste. Das Konto liegt oben rechts in der App-Leiste.
- **Live-Training:** Vollbild ohne Hauptnavigation, damit im Studio nichts ablenkt. Ist eine Einheit aktiv, zeigt Start sie zuoberst zum Fortsetzen an (#FA-5.9).
- **Sitzung:** Beim Laden fragt das Frontend `GET /api/users/me` ab. Ein 401 führt aus jeder Ansicht zu «Anmelden», danach geht es zur ursprünglichen Ansicht zurück.

Die Übergänge zwischen den Ansichten zeigt der Screen-Flow (`diagramme/screen-flow.drawio`).

## Screens

| Screen | Pfad | Wichtigste Elemente | SAF |
|---|---|---|---|
| Anmelden | `/anmelden` | E-Mail, Passwort mit Anzeigen-Knopf, Links zu Registrieren und Datenschutz. Bei Fehlern eine allgemeine Meldung, bei einer Sperre der Hinweis auf 15 Minuten. | SAF2 |
| Registrieren | `/registrieren` | E-Mail, Passwort (15 bis 64 Zeichen, Einfügen erlaubt, anzeigbar), Link zum Datenschutz. Danach direkt angemeldet. | SAF1 |
| Datenschutz | `/datenschutz` | Statischer Text, ohne Anmeldung erreichbar (#FA-1.4) | – |
| Start | `/` | Aktives Training fortsetzen, nächstes Training mit Start-Knopf (#NFA-4.1), Liste der geplanten Trainings (vergangene zuoberst und markiert), Knöpfe «Einplanen» und «Spontan starten» | SAF7, SAF8 |
| Training einplanen | Dialog | Routine auswählen, Datum ab heute. Derselbe Dialog dient zum Ändern. | SAF7 |
| Live-Training | `/training` | Pro Übung eine Karte mit den erfassten Sätzen (Nr., kg, Wdh., Pause) und einer vorbelegten Eingabezeile. «Satz speichern» braucht nur einen Tipp. Antippen eines Satzes öffnet Ändern, Verschieben und Löschen. Dazu ein Notizfeld, unten «Abschliessen» und «Abbrechen». | SAF8 |
| Routinen | `/routinen` | Liste der Routinen mit Anzahl Übungen, Knopf «Neue Routine» | SAF6 |
| Routine bearbeiten | `/routinen/:id` | Name, Übungsliste mit «nach oben», «nach unten» und Entfernen, Übung aus dem Katalog hinzufügen. Löschen mit Hinweis, dass geplante Trainings mitgelöscht werden. | SAF6 |
| Übungen | `/uebungen` | Alphabetische Liste mit Muskelgruppe, Knopf «Neue Übung» | SAF5 |
| Übung bearbeiten | Dialog | Name, Muskelgruppe (Auswahl), Beschreibung, Löschen | SAF5 |
| Verlauf | `/verlauf` | Routinen mit abgeschlossenen Einheiten, gelöschte Routinen markiert | SAF9 |
| Einheiten einer Routine | `/verlauf/:routineId` | Abgeschlossene Einheiten mit Startdatum, neueste zuerst | SAF9 |
| Einheit im Detail | `/verlauf/einheit/:id` | Übungen in Reihenfolge mit ihren Sätzen, Notiz, Löschen | SAF9 |
| Konto | `/konto` | Vor- und Nachname, E-Mail und Passwort ändern (Dialoge mit Passwortbestätigung), Abmelden, Konto löschen | SAF3, SAF4 |

## Hauptflows

1. **Einstieg:** Anmelden oder Registrieren → Start
2. **Vorbereiten:** Übung erfassen → Routine zusammenstellen → Training einplanen
3. **Trainieren:** Start → Training starten (geplant oder spontan) → Live-Training → Abschliessen → Einheit im Detail. Abbrechen führt zurück zu Start.
4. **Nachschauen:** Verlauf → Routine → Einheit
5. **Konto:** Konto → Abmelden oder Konto löschen → Anmelden

Die Abläufe SAF7 und SAF8 sind im Kapitel «Abläufe» als Aktivitätsdiagramme beschrieben.

## Wichtige Elemente

Die Elemente setzen die Gestaltungsgrundsätze mit Standardkomponenten aus Material UI um.

| Element | Einsatz | Bezug |
|---|---|---|
| Untere Navigation, Seitenleiste | Wechsel zwischen den vier Bereichen | Mobile-First |
| Dialog | Kurze Formulare und jede Bestätigung vor dem Löschen | #NFA-7.5 |
| Snackbar und Alert | Erfolg kurz bestätigen, Fehler mit der `message` aus dem Fehlerformat anzeigen | #NFA-7.4 |
| Feldfehler | React Hook Form zeigt Fehler direkt beim Feld, die Eingabe bleibt erhalten | #NFA-5.2 |
| Satz-Eingabe | Drei Zahlenfelder mit numerischer Tastatur, vorbelegt mit dem Vorschlag, grosser Speichern-Knopf | #FA-5.5, #NFA-5.3, #NFA-5.4 |
| Schaltflächen | Mindestens 44 × 44 Pixel, pro Ansicht eine hervorgehobene Hauptaktion | #NFA-7.3 |
| Status-Markierung | «vergangen» bei geplanten Trainings, «gelöscht» bei Routinen im Verlauf | #NFA-4.1, #FA-6.3 |
| Leerer Zustand | Hinweis mit dem nächsten Schritt, z. B. «Noch keine Übungen. Erste Übung erfassen.» | Benutzerführung |
| Formate | Deutsche Texte, Datum TT.MM.JJJJ, Gewicht in kg | #NFA-7.11 |

## Frontend-Struktur

```text
src/
├─ api/         fetch-Client, je Fachbereich ein Modul, TypeScript-Typen der DTOs
├─ pages/       eine Komponente pro Screen
├─ components/  wiederverwendbare Bausteine (Layout, Satz-Eingabe, Bestätigungsdialog)
└─ App.tsx      Routen mit React Router, Schutz der angemeldeten Bereiche
```

Der fetch-Client setzt den JSON-Header, liest das einheitliche Fehlerformat und leitet bei 401 zur Anmeldung. Das Cookie schickt der Browser selbst mit, weil Frontend und API auf derselben Domain laufen (#NFA-7.18).
