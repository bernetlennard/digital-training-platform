# Business Regeln

Alle Regeln werden verbindlich im Backend geprüft (L6, #NFA-7.9). Jede Regel gehört zu genau einer Stufe:

| Stufe | Prüft | Fehler |
|---|---|---|
| Controller | Pflichtfelder, Formate und Wertebereiche der Request-DTOs mit Bean Validation | 400 |
| Service | Regeln, die vom Datenbestand, vom Status oder vom aktuellen Datum abhängen | 401, 404, 409, 422, 429 |
| Datenbank | NOT NULL, UNIQUE, CHECK und Fremdschlüssel als letzte Absicherung (siehe Datenmodell) | 500 |

Die Formatregeln stehen bei den Request-DTOs im Kapitel REST-Schnittstelle. Dazu gehört auch, dass eine Routine mindestens eine Übung hat (#FA-3.1), weil sich das allein aus dem Request ergibt. Gewichte haben höchstens zwei Nachkommastellen (#FA-5.3). NUMERIC(6,2) würde mehr Stellen nur runden, deshalb weist der Controller sie ab.

Für alle Service-Regeln gilt:

- **Besitz:** Ein Service lädt Datensätze immer zusammen mit der Benutzer-ID aus dem Token. Fremde oder nicht vorhandene Datensätze ergeben 404 (L17, #NFA-7.6). Das gilt auch, wenn ein Request auf eine soft-gelöschte Übung oder Routine verweist.
- **Datum:** Was «heute» ist, bestimmt eine zentrale `Clock` mit der Zeitzone Europe/Zurich. Im Unit-Test lässt sie sich durch eine feste Uhr ersetzen.

## Regeln im Service

| Regel | Anforderung | Fehler |
|---|---|---|
| **Benutzerverwaltung** | | |
| Die E-Mail-Adresse wird getrimmt und klein geschrieben gespeichert. Pro Adresse gibt es höchstens ein Konto. | #FA-1.3 | 409 |
| Beim Login erhalten eine unbekannte E-Mail-Adresse und ein falsches Passwort dieselbe Antwort. | #NFA-1.4 | 401 |
| Nach 5 Fehlversuchen in Folge ist die E-Mail-Adresse 15 Minuten gesperrt, auch wenn es dazu kein Konto gibt. | #NFA-1.3 | 429 |
| E-Mail-Adresse ändern, Passwort ändern und Konto löschen verlangen das aktuelle Passwort. | #FA-1.8 bis #FA-1.10 | 401 |
| **Übungskatalog** | | |
| Der Name ist je Sportler eindeutig, ohne Unterscheidung von Gross- und Kleinschreibung. Soft-gelöschte Übungen zählen nicht. | #FA-2.5 | 409 |
| Eine Übung kann erst soft-gelöscht werden, wenn sie in keiner nicht gelöschten Routine mehr vorkommt. | #FA-2.6 | 409 |
| **Trainingsroutinen** | | |
| Der Name ist eindeutig, nach derselben Regel wie bei den Übungen. | #FA-3.4 | 409 |
| Jede Übung kommt höchstens einmal vor. Die Positionen vergibt das System lückenlos ab 1, in der Reihenfolge des Requests. | #FA-3.1 | 422 |
| **Live-Protokollierung** | | |
| Sätze lassen sich nur in der aktiven Einheit erfassen, ändern, verschieben und löschen. | #FA-5.4, #FA-6.5 | 404 |
| Die Satznummern sind lückenlos ab 1, auch nach dem Löschen oder Verschieben eines Satzes. Die Zielposition muss innerhalb der vorhandenen Sätze liegen. | #FA-5.2, #FA-5.4 | 422 |
| Vorbelegt wird der Satz mit gleicher Nummer aus der letzten abgeschlossenen Einheit mit dieser Übung. Fehlt er, gilt der vorherige Satz der aktuellen Einheit, sonst bleibt das Feld leer. Soft-gelöschte Einheiten zählen nicht. | #FA-5.5 | – |

## Statusmodell der Trainingseinheit

Den Status einer Einheit ändern nur die Methoden der Entität `TrainingSession`. Einen Setter für den Status gibt es nicht. So prüft jeder Übergang seine Bedingung selbst, egal welcher Service ihn auslöst.

| Aktion | Status | Bedingung (Fehler) | Wirkung | Anforderung |
|---|---|---|---|---|
| Einplanen | – → geplant | Datum heute oder später (422) | Routine und `planned_date` setzen | #FA-4.1, #FA-4.2 |
| Ändern | geplant | Nur im Status «geplant» (404), Datum heute oder später (422) | Routine und `planned_date` ersetzen | #FA-4.4 |
| Starten | geplant → aktiv | Keine andere Einheit aktiv (409) | Übungen der Routine kopieren, `started_at` setzen | #FA-5.1, #FA-3.5 |
| Spontan starten | – → aktiv | Keine andere Einheit aktiv (409) | Wie Starten, aber ohne `planned_date` | #FA-5.1 |
| Abschliessen | aktiv → abgeschlossen | Mindestens ein Satz (409) | Übungen ohne Satz löschen, `finished_at` setzen | #FA-5.7 |
| Abbrechen | aktiv → geplant | – | Kopierte Übungen und Sätze löschen, `started_at` leeren. Eine spontane Einheit wird ganz gelöscht. | #FA-5.8 |

Daraus folgen die Zeiten pro Status:

- «geplant» hat ein `planned_date`, aber weder `started_at` noch `finished_at`.
- «aktiv» hat nur `started_at`.
- «abgeschlossen» hat beide Zeiten, und `finished_at` liegt nicht vor `started_at`.

Eine abgeschlossene Einheit ist schreibgeschützt und kann nur noch soft-gelöscht werden (#FA-6.4, #FA-6.5). Die übrigen Löschregeln stehen unter «Umsetzung der Löschung».
