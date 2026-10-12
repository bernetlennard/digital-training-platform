# Entwicklung

Die Entwicklung erfolgt nach Test Driven Development (TDD). Das ist die Massnahme zum Risiko R10 «Bias beim Testing»: Jede Funktion entsteht aus einem vorher festgelegten Testfall.

## Vorgehen nach TDD

1. **Grobstruktur aus den Klassendiagrammen:** Alle Klassen sind mit Attributen und Methodensignaturen angelegt. Die Methoden enthalten nur ein TODO mit Verweis auf die Anforderung. Das Projekt kompiliert, jeder neue Test ist aber zuerst rot.
2. **Definitive Testfälle:** Vor der Umsetzung werden die Testfälle in diesem Kapitel festgelegt. Sie sind die Definition of Done: Eine Funktion ist fertig, wenn ihre Testfälle grün sind.
3. **Red, Green, Refactor:** Test schreiben (rot), mit möglichst wenig Code umsetzen (grün), danach aufräumen ohne das Verhalten zu ändern. Nach jedem grünen Zyklus folgt ein Commit.

```java
@Test
@DisplayName("UT-14 Abschliessen ohne Satz wird abgewiesen")
void finish_withoutSets_throwsConflict() {
    TrainingSession session = new TrainingSession(USER_A, ROUTINE_ID, TODAY);
    session.start(NOW, List.of(BENCH_PRESS, SQUAT));

    assertThatThrownBy(() -> session.finish(NOW))
            .isInstanceOf(ConflictException.class);
    assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
}
```

Umgesetzt wird von innen nach aussen (Entität, Service, Controller) und in der Reihenfolge der Abhängigkeiten: Benutzerverwaltung, Übungen, Routinen, Planung, Live-Protokollierung, Historie.

## Testanleitung

- **Unit-Tests** mit JUnit, Mockito und AssertJ prüfen Entitäten und Services. Controller, Validierung und Security werden mit `@WebMvcTest` getestet. Die Uhr ist fest eingestellt, damit Datumsregeln reproduzierbar sind.
- **Ein Testfall** beschreibt eine Geschäftsregel und wird mit einem oder mehreren Testmethoden über alle betroffenen Schichten umgesetzt. Jede Methode trägt die ID des Testfalls im `@DisplayName`.
- **Manuelle Systemtests** laufen über HTTP-Anfragen gegen das lokale Backend mit PostgreSQL. Sie prüfen, was nur mit der echten Datenbank geht (Kaskaden, Constraints, Abfragen).
- **Durchführung:** Die Unit-Tests laufen lokal mit `mvnw test` und bei jedem Pull Request in der Pipeline. Ein roter Test blockiert Merge und Deployment (#NFA-7.22). Manuelle Tests werden im Testprotokoll festgehalten.

## Unit-Tests Backend

| ID | Testfall | Erwartetes Ergebnis | Anforderung |
|---|---|---|---|
| **Querschnitt** | | | |
| UT-01 | Ungültige Eingaben, fachliche Fehler und unerwartete Fehler | 400 mit Feldfehlern, 401/404/409/422/429 bzw. 500 im einheitlichen Fehlerformat, ohne Stacktrace und interne Details | #NFA-7.9, #NFA-7.17 |
| UT-02 | Zugriff ohne, mit abgelaufenem oder falsch signiertem Token; Zugriff auf fremde oder gelöschte Datensätze | 401 (ausser Registrierung, Login, Health-Check); 404 bei fremden Daten; die Benutzer-ID kommt immer aus dem Token | #NFA-1.2, #NFA-7.6, #NFA-7.7 |
| **FT1 Benutzerverwaltung** | | | |
| UT-03 | Registrieren und anmelden | E-Mail getrimmt, klein geschrieben und eindeutig (409); Passwort 15–64 Zeichen (400); Argon2id-Hash; Cookie `HttpOnly`, `Secure`, `SameSite=Strict`, 8 Stunden | #FA-1.1 bis #FA-1.3, #FA-1.5, #NFA-1.1 |
| UT-04 | Falsches Passwort, unbekannte E-Mail, 5 Fehlversuche in Folge | identische 401-Antwort; danach 15 Minuten gesperrt (429), auch für E-Mails ohne Konto | #NFA-1.3, #NFA-1.4 |
| UT-05 | Profil ändern, E-Mail und Passwort ändern, Konto löschen, abmelden | Änderungen gespeichert; E-Mail, Passwort und Löschung nur mit korrektem Passwort (401); Löschung in einer Transaktion, danach wird das alte Token abgewiesen | #FA-1.6 bis #FA-1.10, #NFA-1.5, #NFA-1.6 |
| **FT2 Übungskatalog** | | | |
| UT-06 | Übung anlegen und ändern | Name und Muskelgruppe Pflicht (400); Name je Sportler eindeutig ohne Gross-/Kleinschreibung (409), gelöschte Übungen zählen nicht | #FA-2.1, #FA-2.4, #FA-2.5 |
| UT-07 | Übung löschen | soft gelöscht; in einer nicht gelöschten Routine verwendet ergibt 409 | #FA-2.6 |
| **FT3 Trainingsroutinen** | | | |
| UT-08 | Routine anlegen und ändern | mindestens eine Übung (400), keine Übung doppelt (422), Positionen 1..n in der Reihenfolge des Requests, Name eindeutig (409) | #FA-3.1, #FA-3.3, #FA-3.4, #FA-3.6 |
| UT-09 | Routine löschen bzw. ändern nach dem Start einer Einheit | soft gelöscht, geplante Einheiten hart gelöscht; gestartete Einheiten behalten ihre kopierten Übungen | #FA-3.5, #FA-3.7 |
| **FT4 Trainingsplanung** | | | |
| UT-10 | Einheit einplanen, ändern und entfernen | Datum ab heute nach Europe/Zurich (sonst 422); überfällige Einheiten nur noch löschbar; nur Einheiten mit Status «geplant» änderbar (sonst 404) | #FA-4.1, #FA-4.2, #FA-4.4, #FA-4.5 |
| **FT5 Live-Protokollierung** | | | |
| UT-11 | Geplante Einheit oder Routine spontan starten | Status «aktiv», Übungen der Routine in Reihenfolge kopiert; zweite aktive Einheit ergibt 409 | #FA-5.1 |
| UT-12 | Sätze erfassen, ändern, verschieben und löschen | Satznummern immer lückenlos ab 1; Gewicht 0–1000 kg mit max. 2 Nachkommastellen, 1–100 Wiederholungen, Pause 0–3600 s (400); Zielposition ausserhalb (422) | #FA-5.2 bis #FA-5.4 |
| UT-13 | Vorschlag für den nächsten Satz | Satz mit gleicher Nummer aus der letzten abgeschlossenen Einheit mit dieser Übung, sonst vorheriger Satz der aktuellen Einheit, sonst leer | #FA-5.5 |
| UT-14 | Einheit abschliessen bzw. abbrechen | Abschluss nur mit mindestens einem Satz (409), Übungen ohne Satz entfernt; Abbruch setzt geplante Einheit zurück, spontane wird gelöscht | #FA-5.7, #FA-5.8 |
| **FT6 Trainingshistorie** | | | |
| UT-15 | Historie lesen und Einheit löschen | nur abgeschlossene Einheiten, neueste zuerst, auch für gelöschte Routinen; Einheit wird soft gelöscht; abgeschlossene Einheiten sind nicht mehr bearbeitbar (404) | #FA-6.1, #FA-6.3 bis #FA-6.5 |

*Tabelle: Unit-Tests Backend*

## Manuelle Systemtests Backend

| ID | Testfall | Erwartetes Ergebnis | Anforderung |
|---|---|---|---|
| ST-01 | Registrieren, anmelden, abmelden, Konto mit Trainingsdaten löschen | alle Schritte erfolgreich; danach keine Daten des Sportlers mehr in der Datenbank, andere Sportler unverändert | #FA-1.1, #FA-1.5, #FA-1.6, #FA-1.10, #NFA-1.6 |
| ST-02 | Übung und Routine anlegen, Training einplanen, starten, Sätze erfassen, verschieben und löschen, abschliessen, nächste Einheit starten | Einheit erscheint korrekt in der Historie, die nächste Einheit zeigt die Vorschläge der letzten | #FA-2.1 bis #FA-6.2 |
| ST-03 | Zweiter Sportler greift auf Daten des ersten zu | jeweils 404, keine Daten verändert | #NFA-7.6 |
| ST-04 | Übung und Routine mit bestehender Historie löschen, gleichen Namen neu anlegen | Historie bleibt vollständig, gelöschte Routine ist markiert, Name ist wieder verwendbar | #FA-2.5, #FA-2.6, #FA-3.7, #FA-6.3 |
| ST-05 | Satz speichern; Historie mit 300 Einheiten laden | unter 1 Sekunde bzw. höchstens 2 Sekunden | #NFA-5.5, #NFA-6.1 |

*Tabelle: Manuelle Systemtests Backend*

## Frontend, Smoke-Tests und Reviews

- **Frontend (offen):** Unit-Tests mit Vitest und React Testing Library für Hilfsfunktionen und zentrale Komponenten. Die Anforderungen zur Bedienung (z. B. #NFA-5.3, #NFA-7.1, #NFA-7.3) werden als manuelle Systemtests über die Oberfläche ergänzt, sobald das Frontend steht.
- **Smoke-Test nach jedem Deployment:** Health-Endpunkt über HTTPS (automatisch), Umleitung von HTTP auf HTTPS, nur Ports 22, 80 und 443 offen, Swagger UI nicht erreichbar, Login und ein kurzes Training (#NFA-7.8, #NFA-7.16, #NFA-7.19, #NFA-7.23).
- **Review vor der Abgabe:** Schichtenarchitektur, keine Secrets im Repository, fixierte Versionen, Branch-Schutz und Pipeline, Log-Aufbewahrung 14 Tage, keine Gesundheitsdaten (#NFA-7.12 bis #NFA-7.15, #NFA-7.20 bis #NFA-7.22, #NFA-7.24).

## Testprotokoll

| Datum | Commit | ID | Ergebnis | Bemerkung |
|---|---|---|---|---|
| TT.MM.JJJJ | `abc1234` | ST-01 | bestanden / nicht bestanden | |
