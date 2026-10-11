# REST-Schnittstelle

Das Backend stellt seine gesamte Funktionalität über eine REST-Schnittstelle bereit. Das Frontend greift ausschliesslich über HTTP mit JSON-Daten darauf zu. Dieses Kapitel gibt eine Übersicht über die Endpunkte, die übertragenen Datenobjekte, das Fehlerformat und die Authentifizierung. Die vollständige, maschinenlesbare Beschreibung aller Endpunkte enthält die OpenAPI-Spezifikation im Anhang «OpenAPI-Spezifikation».

## Grundsätze

Für alle Endpunkte gelten dieselben Konventionen:

- **Präfix und Gliederung:** Alle Endpunkte liegen unter `/api`. Darunter ist die Schnittstelle nach Fachbereichen gegliedert, jeder Fachbereich entspricht einem Controller.
- **Ressourcen:** Sammlungen werden mit Substantiven im Plural adressiert (`/api/exercises`, `/api/routines`), einzelne Ressourcen über ihre ID im Pfad (`/api/routines/{routineId}`).
- **Methoden:** GET liest, POST legt an, PUT ersetzt eine Ressource vollständig und DELETE löscht sie. PATCH wird nicht verwendet. Anlegen und Ändern verwenden deshalb dasselbe Request-DTO.
- **Aktionen:** Die Zustandsübergänge einer Trainingseinheit (starten, abschliessen, abbrechen) lassen sich nicht sinnvoll als Anlegen, Ändern oder Löschen abbilden. Sie sind als eigene Aktions-Endpunkte mit POST umgesetzt (`…/start`, `…/finish`, `…/cancel`).
- **Feste Pfade statt IDs:** Ressourcen, die es pro Sportler nur einmal gibt, haben einen festen Pfad. `/api/users/me` steht für das eigene Konto, `/api/sessions/active` für die aktive Einheit, von der es höchstens eine geben kann (#FA-5.1). Der Client muss dafür keine ID kennen.
- **Besitz:** Die Benutzer-ID wird nie aus dem Pfad oder dem Body gelesen, sondern immer aus dem geprüften Token (siehe Abschnitt «Authentifizierung»). Alle Services filtern nach dieser ID. Ein Sportler kann deshalb nur auf seine eigenen Daten zugreifen. Die ID einer fremden Ressource wird wie eine nicht existierende behandelt und mit 404 beantwortet, sodass sich aus der Antwort nicht ablesen lässt, ob die Ressource existiert.

Die Schnittstelle verwendet die folgenden Statuscodes:

| Code | Bedeutung in dieser Schnittstelle |
|---|---|
| 200 OK | Erfolgreich, die Antwort enthält einen Body |
| 201 Created | Ressource angelegt, der Body enthält die neue Ressource |
| 204 No Content | Erfolgreich, die Antwort enthält keinen Body |
| 400 Bad Request | Das Request-DTO verletzt eine Validierungsregel |
| 401 Unauthorized | Kein oder ungültiges Token bzw. falsche Zugangsdaten |
| 404 Not Found | Die Ressource existiert nicht oder gehört einem anderen Sportler |
| 409 Conflict | Widerspruch zum gespeicherten Datenbestand, z. B. ein bereits vergebener Name |
| 422 Unprocessable Content | Fachliche Regel verletzt, die sich aus dem Request allein ergibt, z. B. ein Datum in der Vergangenheit |
| 429 Too Many Requests | Login nach zu vielen Fehlversuchen vorübergehend gesperrt |
| 500 Internal Server Error | Unerwarteter Fehler auf dem Server |

*Tabelle: Verwendete HTTP-Statuscodes*

Die Abgrenzung zwischen 409 und 422 folgt einer einfachen Regel: Ein 409 entsteht erst durch den Vergleich mit gespeicherten Daten (der Name existiert bereits, es läuft schon eine Einheit). Ein 422 lässt sich am Request selbst erkennen (das Datum liegt vor heute, eine Übung kommt doppelt vor).

## Endpunkte

Die folgende Tabelle listet alle Endpunkte nach Fachbereich. In der Spalte «Statuscodes» steht zuerst der Erfolgsfall, danach die fachlich möglichen Fehler.

| Methode | Pfad | Zweck | Statuscodes | Auth |
|---|---|---|---|---|
| **Authentifizierung** | | | | |
| POST | `/api/auth/register` | Konto anlegen und anmelden | 201, 400, 409 | nein |
| POST | `/api/auth/login` | Anmelden | 200, 400, 401, 429 | nein |
| POST | `/api/auth/logout` | Abmelden, Cookie löschen | 204 | ja |
| **Benutzerkonto** | | | | |
| GET | `/api/users/me` | Eigenes Profil lesen | 200 | ja |
| PUT | `/api/users/me` | Vor- und Nachname ändern | 200, 400 | ja |
| PUT | `/api/users/me/email` | E-Mail-Adresse ändern | 200, 400, 401, 409 | ja |
| PUT | `/api/users/me/password` | Passwort ändern | 204, 400, 401 | ja |
| DELETE | `/api/users/me` | Konto mit allen Daten löschen | 204, 400, 401 | ja |
| **Übungen** | | | | |
| GET | `/api/exercises` | Eigene Übungen auflisten | 200 | ja |
| GET | `/api/exercises/muscle-groups` | Muskelgruppen auflisten | 200 | ja |
| POST | `/api/exercises` | Übung anlegen | 201, 400, 409 | ja |
| PUT | `/api/exercises/{exerciseId}` | Übung ändern | 200, 400, 404, 409 | ja |
| DELETE | `/api/exercises/{exerciseId}` | Übung löschen | 204, 404, 409 | ja |
| **Routinen** | | | | |
| GET | `/api/routines` | Eigene Routinen auflisten | 200 | ja |
| GET | `/api/routines/{routineId}` | Routine mit Übungen lesen | 200, 404 | ja |
| POST | `/api/routines` | Routine anlegen | 201, 400, 404, 409, 422 | ja |
| PUT | `/api/routines/{routineId}` | Routine ändern | 200, 400, 404, 409, 422 | ja |
| DELETE | `/api/routines/{routineId}` | Routine löschen | 204, 404 | ja |
| **Trainingsplanung** | | | | |
| GET | `/api/sessions/planned` | Geplante Einheiten auflisten | 200 | ja |
| POST | `/api/sessions/planned` | Einheit planen | 201, 400, 404, 422 | ja |
| PUT | `/api/sessions/planned/{sessionId}` | Geplante Einheit verschieben | 200, 400, 404, 422 | ja |
| DELETE | `/api/sessions/planned/{sessionId}` | Geplante Einheit löschen | 204, 404 | ja |
| **Live-Training** | | | | |
| POST | `/api/sessions/planned/{sessionId}/start` | Geplante Einheit starten | 200, 404, 409 | ja |
| POST | `/api/sessions` | Spontane Einheit starten | 201, 400, 404, 409 | ja |
| GET | `/api/sessions/active` | Aktive Einheit lesen | 200, 204 | ja |
| PUT | `/api/sessions/active/note` | Notiz zur Einheit setzen | 204, 400, 404 | ja |
| POST | `/api/sessions/active/exercises/{sessionExerciseId}/sets` | Satz erfassen | 201, 400, 404 | ja |
| PUT | `/api/sessions/active/sets/{setId}` | Satz ändern | 200, 400, 404 | ja |
| PUT | `/api/sessions/active/sets/{setId}/position` | Satz verschieben | 200, 400, 404, 422 | ja |
| DELETE | `/api/sessions/active/sets/{setId}` | Satz löschen | 200, 404 | ja |
| POST | `/api/sessions/active/finish` | Einheit abschliessen | 204, 404, 409 | ja |
| POST | `/api/sessions/active/cancel` | Einheit abbrechen | 204, 404 | ja |
| **Verlauf** | | | | |
| GET | `/api/history/routines` | Routinen mit abgeschlossenen Einheiten auflisten | 200 | ja |
| GET | `/api/history/routines/{routineId}/sessions` | Abgeschlossene Einheiten einer Routine auflisten | 200 | ja |
| GET | `/api/history/sessions/{sessionId}` | Abgeschlossene Einheit im Detail lesen | 200, 404 | ja |
| DELETE | `/api/history/sessions/{sessionId}` | Einheit aus dem Verlauf löschen | 204, 404 | ja |

*Tabelle: Endpunkte der REST-Schnittstelle*

Zwei Statuscodes sind nicht einzeln aufgeführt: Jeder Endpunkt mit Auth = ja antwortet mit 401, wenn das Token fehlt oder ungültig ist, und jeder Endpunkt kann bei einem unerwarteten Fehler mit 500 antworten. Ein 401 in der Tabelle steht deshalb immer für falsche Zugangsdaten oder ein falsches Bestätigungspasswort.

Zu einzelnen Endpunkten:

- **Muskelgruppen** sind Stammdaten mit festen IDs. Sie können nur gelesen werden.
- **Löschen von Übungen und Routinen** ist als Soft Delete umgesetzt, damit abgeschlossene Einheiten im Verlauf vollständig bleiben. Für den Client verhält sich der Aufruf wie ein normales Löschen (204), die Ressource ist danach nicht mehr abrufbar. Eine Übung, die noch in einer aktiven Routine verwendet wird, kann nicht gelöscht werden (409, #FA-2.6). Wird eine Routine gelöscht, werden ihre geplanten Einheiten mitgelöscht.
- **Starten einer Einheit:** Der Start einer geplanten Einheit erzeugt keine neue Ressource, sondern ändert den Status einer bestehenden. Er antwortet deshalb mit 200. Eine spontane Einheit wird dagegen neu angelegt und mit 201 beantwortet. Läuft bereits eine Einheit, schlagen beide Aufrufe mit 409 fehl (#FA-5.1).
- **Aktive Einheit lesen:** Dass gerade kein Training läuft, ist der Normalfall und kein Fehler. `GET /api/sessions/active` antwortet in diesem Fall mit 204 statt mit 404.
- **Sätze** gehören immer zur aktiven Einheit, deshalb enthalten die Pfade keine Session-ID. Ein neuer Satz wird über die Übung der Einheit (`sessionExerciseId`) angelegt, bestehende Sätze werden direkt über ihre `setId` angesprochen.
- **Abbrechen** setzt eine geplante Einheit in den Status «geplant» zurück und löscht eine spontane Einheit vollständig (#FA-5.8). **Abschliessen** setzt den Endzeitpunkt (#FA-5.7).
- **Verlauf:** Abgeschlossene Einheiten können aus dem Verlauf gelöscht werden (#FA-6.5). Gelöschte Routinen erscheinen weiterhin im Verlauf, sofern zu ihnen abgeschlossene Einheiten existieren.

## Datenübertragungsobjekte

### Grundsatz

Die JPA-Entitäten verlassen die Service-Schicht nie. Controller nehmen ausschliesslich Request-DTOs entgegen und geben Response-DTOs zurück. Alle DTOs sind als Java-Records umgesetzt und liegen pro Fachbereich im Paket `service.<bereich>.dto`. Die Abbildung zwischen Entität und DTO übernehmen statische Mapper-Klassen (`UserMapper`, `ExerciseMapper`, `RoutineMapper`, `TrainingMapper`).

Diese Trennung hat mehrere Vorteile:

- Das Datenbankschema kann sich ändern, ohne dass sich die Schnittstelle ändert, und umgekehrt.
- Sensible Felder gelangen nicht versehentlich in eine Antwort. So enthält `UserResponse` den Passwort-Hash nicht.
- Bei der Serialisierung werden keine Lazy-Beziehungen nachgeladen, und zyklische Referenzen zwischen Entitäten können nicht entstehen.
- Records sind unveränderlich und kommen ohne Boilerplate-Code aus.

### Request-DTOs und Validierung

Request-DTOs werden mit Bean Validation geprüft. Der Controller markiert den Parameter mit `@Valid`, sodass ungültige Requests mit 400 abgewiesen werden, bevor der Service aufgerufen wird. Die Regeln entsprechen den Längen und Prüfregeln des Datenbankschemas (Kapitel «Konzeption > Datenmodell»). Ein ungültiger Wert wird dadurch schon an der Schnittstelle mit einer verständlichen Meldung abgewiesen und nicht erst durch einen Constraint der Datenbank, der zu einem 500 führen würde.

| DTO | Felder | Validierung | Verwendung |
|---|---|---|---|
| `RegisterRequest` | `email`, `password` | `email`: Pflicht, E-Mail-Format, max. 254 Zeichen; `password`: Pflicht, Passwortrichtlinie | `POST /auth/register` |
| `LoginRequest` | `email`, `password` | beide Pflicht | `POST /auth/login` |
| `UpdateProfileRequest` | `firstName`, `lastName` | optional, je max. 100 Zeichen | `PUT /users/me` |
| `ChangeEmailRequest` | `newEmail`, `password` | `newEmail`: Pflicht, E-Mail-Format, max. 254 Zeichen; `password`: Pflicht | `PUT /users/me/email` |
| `ChangePasswordRequest` | `currentPassword`, `newPassword` | beide Pflicht; `newPassword`: Passwortrichtlinie | `PUT /users/me/password` |
| `DeleteAccountRequest` | `password` | Pflicht | `DELETE /users/me` |
| `ExerciseRequest` | `name`, `muscleGroupId`, `description` | `name`: Pflicht, max. 100 Zeichen; `description`: optional, max. 1000 Zeichen | `POST`, `PUT /exercises` |
| `RoutineRequest` | `name`, `exerciseIds` | `name`: Pflicht, max. 100 Zeichen; `exerciseIds`: mind. ein Eintrag, Reihenfolge = Position | `POST`, `PUT /routines` |
| `PlanSessionRequest` | `routineId`, `plannedDate` | beide Pflicht | `POST`, `PUT /sessions/planned` |
| `StartSessionRequest` | `routineId` | Pflicht | `POST /sessions` |
| `NoteRequest` | `note` | max. 1000 Zeichen | `PUT /sessions/active/note` |
| `SetRequest` | `weightKg`, `repetitions`, `restSeconds` | `weightKg`: Pflicht, 0–1000, max. 2 Nachkommastellen; `repetitions`: 1–100; `restSeconds`: optional, 0–3600 | Satz erfassen und ändern |
| `MoveSetRequest` | `newSetNumber` | mind. 1 | Satz verschieben |

*Tabelle: Request-DTOs (Pfade ohne Präfix `/api`)*

Prüfungen, die gespeicherte Daten benötigen, kann Bean Validation nicht leisten. Sie finden im Service statt, etwa die Eindeutigkeit eines Namens (409), die Existenz der referenzierten Übungen (404) oder ein geplantes Datum, das nicht in der Vergangenheit liegen darf (422, #FA-4.2).

### Response-DTOs

| DTO | Felder | Verwendung |
|---|---|---|
| `UserResponse` | `id`, `email`, `firstName`, `lastName` | Authentifizierung, Benutzerkonto |
| `MuscleGroupResponse` | `id`, `name` | Muskelgruppen |
| `ExerciseResponse` | `id`, `name`, `muscleGroupId`, `muscleGroupName`, `description` | Übungen |
| `RoutineResponse` | `id`, `name`, `exercises` (Liste von `RoutineExerciseResponse`) | Routinen |
| `RoutineExerciseResponse` | `position`, `exerciseId`, `name`, `muscleGroupName` | in `RoutineResponse` |
| `PlannedSessionResponse` | `id`, `routineId`, `routineName`, `plannedDate` | Trainingsplanung |
| `SessionResponse` | `id`, `routineId`, `routineName`, `routineDeleted`, `status`, `startedAt`, `finishedAt`, `note`, `exercises` (Liste von `SessionExerciseResponse`) | Live-Training, Detail im Verlauf |
| `SessionExerciseResponse` | `id`, `exerciseId`, `exerciseName`, `position`, `sets` (Liste von `SetResponse`), `nextSet` (`SetSuggestion`, optional) | Satz-Endpunkte, in `SessionResponse` |
| `SetResponse` | `id`, `setNumber`, `weightKg`, `repetitions`, `restSeconds` | in `SessionExerciseResponse` |
| `SetSuggestion` | `weightKg`, `repetitions`, `restSeconds` | in `SessionExerciseResponse` |
| `HistoryRoutineResponse` | `routineId`, `name`, `deleted` | Verlauf: Routinen |
| `SessionSummaryResponse` | `id`, `startedAt`, `finishedAt` | Verlauf: Einheiten einer Routine |

*Tabelle: Response-DTOs*

Beim Entwurf der Response-DTOs waren folgende Überlegungen massgebend:

- **Namen statt nur IDs:** Requests referenzieren andere Ressourcen nur über ihre ID (`exerciseIds`, `routineId`). Responses liefern zusätzlich die Namen mit (`exerciseName`, `routineName`, `muscleGroupName`). Das Frontend kann eine Routine oder Einheit damit ohne weitere Requests darstellen.
- **Gelöschte Routinen sichtbar machen:** Weil Routinen nur logisch gelöscht werden, enthalten `SessionResponse` und `HistoryRoutineResponse` ein Kennzeichen (`routineDeleted` bzw. `deleted`). Der Verlauf kann so auch Einheiten gelöschter Routinen anzeigen und diese als gelöscht kennzeichnen.
- **Ganze Übung als Antwort der Satz-Endpunkte:** Erfassen, Ändern, Verschieben und Löschen eines Satzes geben nicht nur den Satz zurück, sondern die ganze Übung der Einheit (`SessionExerciseResponse`). Beim Verschieben und Löschen werden die übrigen Sätze neu nummeriert (#FA-5.4), und der Vorschlag für den nächsten Satz (`nextSet`, #FA-5.5) ändert sich mit jedem Satz. Mit der ganzen Übung kann das Frontend die Ansicht ohne zweiten Request aktualisieren. Aus demselben Grund antwortet auch das Löschen eines Satzes mit 200 und Body statt mit 204.
- **Liste und Detail getrennt:** Die Liste der Einheiten im Verlauf enthält nur Start- und Endzeit (`SessionSummaryResponse`). Die Sätze werden erst für eine einzelne Einheit geladen (`SessionResponse`). Die Listen bleiben dadurch klein.

Für die Datentypen gelten folgende Festlegungen:

| Typ in Java | Darstellung in JSON | Beispiel |
|---|---|---|
| `Long` (IDs) | Zahl | `42` |
| `Instant` (`startedAt`, `finishedAt`) | Zeitpunkt nach ISO 8601 in UTC | `"2026-10-11T16:05:00Z"` |
| `LocalDate` (`plannedDate`) | Datum nach ISO 8601 ohne Uhrzeit | `"2026-10-15"` |
| `BigDecimal` (`weightKg`) | Dezimalzahl, exakt wie in der Datenbank (`NUMERIC(6,2)`) | `82.50` |
| `String` (`status`) | Bezeichnung des Status | `"geplant"`, `"aktiv"`, `"abgeschlossen"` |

*Tabelle: Darstellung der Datentypen*

Ein Beispiel für eine aktive Einheit mit einer Übung und einem erfassten Satz (`GET /api/sessions/active`):

```json
{
  "id": 42,
  "routineId": 7,
  "routineName": "Oberkörper A",
  "routineDeleted": false,
  "status": "aktiv",
  "startedAt": "2026-10-11T16:05:00Z",
  "finishedAt": null,
  "note": null,
  "exercises": [
    {
      "id": 101,
      "exerciseId": 3,
      "exerciseName": "Bankdrücken",
      "position": 1,
      "sets": [
        { "id": 501, "setNumber": 1, "weightKg": 80.00, "repetitions": 8, "restSeconds": 120 }
      ],
      "nextSet": { "weightKg": 80.00, "repetitions": 8, "restSeconds": 120 }
    }
  ]
}
```

## Einheitliches Fehlerformat

Jeder Fehler wird unabhängig von seiner Ursache mit derselben JSON-Struktur beantwortet. Das Frontend kann Fehler dadurch an einer einzigen Stelle auswerten. Die Struktur ist als Record `ErrorResponse` umgesetzt:

```java
public record ErrorResponse(int status, String message, List<String> details) {
}
```

| Feld | Inhalt |
|---|---|
| `status` | HTTP-Statuscode. Er ist redundant zum Header, damit der Body auch allein auswertbar ist. |
| `message` | Kurze, für den Sportler verständliche Meldung |
| `details` | Einzelfehler. Bei Validierungsfehlern steht ein Eintrag pro fehlerhaftem Feld, sonst ist die Liste leer. |

*Tabelle: Felder von ErrorResponse*

Spring bietet mit `ProblemDetail` auch das standardisierte Format nach RFC 9457 an. Das eigene Format ist schlanker, und die Liste der Feldfehler ist darin fester Bestandteil statt einer Erweiterung.

Beispiel für einen Validierungsfehler bei der Registrierung:

```json
{
  "status": 400,
  "message": "Die Eingaben sind ungültig.",
  "details": [
    "email: muss eine korrekt formatierte E-Mail-Adresse sein",
    "password: darf nicht leer sein"
  ]
}
```

Beispiel für einen fachlichen Fehler beim Anlegen einer Übung:

```json
{
  "status": 409,
  "message": "Eine Übung mit diesem Namen existiert bereits.",
  "details": []
}
```

### Entstehung der Fehlerantworten

Fehler entstehen an vier Stellen. Alle werden in das gleiche Format übersetzt.

**1. Fachliche Fehler.** Die Services werfen Unterklassen der abstrakten `DomainException`. Jede Unterklasse legt ihren HTTP-Status selbst fest. Der `GlobalExceptionHandler` (`@RestControllerAdvice`) übersetzt alle fachlichen Fehler mit einer einzigen Methode in eine `ErrorResponse`. Eine neue Fehlerart erfordert deshalb keine Änderung am Handler.

| Exception | Status | Beispiele |
|---|---|---|
| `NotFoundException` | 404 | Routine existiert nicht oder gehört einem anderen Sportler; keine aktive Einheit vorhanden |
| `ConflictException` | 409 | Name einer Übung oder Routine bereits vergeben; E-Mail-Adresse bereits registriert; Übung in aktiver Routine verwendet (#FA-2.6); bereits eine aktive Einheit (#FA-5.1) |
| `BusinessRuleException` | 422 | Geplantes Datum in der Vergangenheit (#FA-4.2) |
| `AuthenticationFailedException` | 401 | Falsche Zugangsdaten beim Login (#NFA-1.4); falsches Passwort bei der Bestätigung einer Kontoänderung |
| `AccountLockedException` | 429 | Login nach fünf Fehlversuchen für 15 Minuten gesperrt (#NFA-1.3) |

*Tabelle: Fachliche Exceptions und ihre Statuscodes*

**2. Validierungsfehler.** Schlägt die Bean Validation fehl, wirft Spring eine `MethodArgumentNotValidException`. Der Handler antwortet mit 400 und schreibt pro Fehler den Feldnamen und die Meldung in `details`. Der abgelehnte Wert wird nie zurückgegeben (#NFA-1.1). So erscheint etwa ein Passwort, das die Passwortrichtlinie verletzt, weder in der Antwort noch im Log.

**3. Unerwartete Fehler.** Alle übrigen Exceptions fängt ein allgemeiner Handler ab. Er protokolliert den Fehler mit Stacktrace auf dem Server und antwortet mit 500 und einer allgemeinen Meldung. Stacktraces, SQL-Meldungen oder Klassennamen gelangen nicht zum Client und geben damit keine Auskunft über den inneren Aufbau der Anwendung.

**4. Fehler der Zugriffskontrolle.** Spring Security weist Requests ohne gültiges Token bereits in der Filterkette ab, also bevor ein Controller erreicht wird. Der `GlobalExceptionHandler` sieht diese Fehler nicht. Dafür ist der `SecurityErrorHandler` zuständig. Er implementiert `AuthenticationEntryPoint` (401) und `AccessDeniedHandler` (403) und schreibt dieselbe `ErrorResponse` als JSON in die Antwort. Abgewiesene Zugriffe werden protokolliert, ohne den Inhalt des Tokens zu loggen (#NFA-1.7). Da die Anwendung keine Rollen kennt, tritt 403 im Normalbetrieb nicht auf, wird der Vollständigkeit halber aber ebenfalls im einheitlichen Format beantwortet.

## Authentifizierung

### Verfahren

Die Authentifizierung ist zustandslos: Der Server führt keine Sessions (`SessionCreationPolicy.STATELESS`). Nach erfolgreicher Registrierung oder Anmeldung stellt er ein JSON Web Token (JWT) aus, das mit HMAC-SHA256 (HS256) signiert ist. Der Schlüssel ist mindestens 256 Bit lang und wird aus der Konfiguration gelesen, im Betrieb aus einer Umgebungsvariable.

| Claim | Inhalt |
|---|---|
| `sub` | ID des Sportlers |
| `iat` | Ausstellungszeitpunkt |
| `exp` | Ablaufzeitpunkt, standardmässig acht Stunden nach Ausstellung (`app.jwt.validity`) |

*Tabelle: Inhalt des JWT*

Das Token enthält bewusst nur die ID und keine weiteren Personendaten wie die E-Mail-Adresse.

Das Token wird nicht im Body übertragen, sondern als Cookie `access_token` gesetzt:

| Attribut | Wert | Zweck |
|---|---|---|
| `HttpOnly` | gesetzt | JavaScript kann das Cookie nicht lesen. Ein eingeschleustes Skript (XSS) kann das Token nicht auslesen. |
| `Secure` | gesetzt | Der Browser sendet das Cookie nur über HTTPS. |
| `SameSite` | `Strict` | Der Browser sendet das Cookie nur bei Requests von derselben Site mit. Das schützt vor Cross-Site Request Forgery (CSRF). |
| `Path` | `/` | Das Cookie gilt für die ganze Anwendung. |
| `Max-Age` | Gültigkeit des Tokens | Cookie und Token laufen gleichzeitig ab. |

*Tabelle: Attribute des Auth-Cookies*

Weil `SameSite=Strict` den Schutz vor CSRF übernimmt, ist der CSRF-Schutz von Spring Security deaktiviert. Voraussetzung dafür ist, dass Frontend und Backend unter derselben Site ausgeliefert werden. Andernfalls würde der Browser das Cookie gar nicht mitsenden.

### Prüfung bei jedem Request

Öffentlich erreichbar sind nur `POST /api/auth/register` und `POST /api/auth/login`. Für alle anderen Endpunkte prüft Spring Security als OAuth2 Resource Server bei jedem Request das Token in vier Schritten:

1. Der `CookieBearerTokenResolver` liest das Token aus dem Cookie `access_token`. Fehlt das Cookie, gilt der Request als nicht authentifiziert.
2. Der `JwtDecoder` prüft die Signatur und den Ablaufzeitpunkt.
3. Der `JwtUserIdConverter` liest die ID aus `sub` und prüft, ob der Sportler noch existiert. Erst dann erzeugt er eine `Authentication` mit der ID als Principal.
4. Der Controller erhält die ID über `@AuthenticationPrincipal Long userId` und gibt sie an den Service weiter.

Schlägt einer der Schritte fehl, antwortet der `SecurityErrorHandler` mit 401.

Da das Frontend das Token nicht lesen kann, erfährt es über `GET /api/users/me`, ob ein Sportler angemeldet ist: 200 liefert das Profil, 401 bedeutet «nicht angemeldet».

### Registrierung

`POST /api/auth/register` legt ein Konto an und meldet den Sportler gleich an.

```json
{ "email": "anna@example.ch", "password": "…" }
```

Der Ablauf:

1. Der Request wird validiert (400 bei Verstoss).
2. Die E-Mail-Adresse wird normalisiert, also getrimmt und in Kleinbuchstaben umgewandelt. Die Datenbank erzwingt die Kleinschreibung zusätzlich mit einer Prüfregel.
3. Ist die Adresse bereits registriert, folgt 409.
4. Das Passwort wird mit Argon2id gehasht und nur als Hash gespeichert.
5. Der Server stellt ein Token aus und antwortet mit 201, der `UserResponse` im Body und dem Cookie im Header `Set-Cookie`.

```http
HTTP/1.1 201 Created
Set-Cookie: access_token=eyJhbGciOiJIUzI1NiJ9…; Path=/; Max-Age=28800; Expires=…; Secure; HttpOnly; SameSite=Strict
Content-Type: application/json

{ "id": 1, "email": "anna@example.ch", "firstName": null, "lastName": null }
```

### Login

`POST /api/auth/login` meldet einen bestehenden Sportler an. Der Request hat denselben Aufbau wie bei der Registrierung.

1. Ist die E-Mail-Adresse gesperrt, folgt sofort 429, ohne dass das Passwort geprüft wird (#NFA-1.3).
2. Das Passwort wird mit dem gespeicherten Argon2id-Hash verglichen.
3. Bei einem Fehlschlag wird der Fehlversuch gezählt, und der Server antwortet mit 401 (#NFA-1.4). Eine unbekannte E-Mail-Adresse und ein falsches Passwort erhalten dieselbe Antwort, damit sich über den Login nicht herausfinden lässt, welche Adressen registriert sind.
4. Nach fünf Fehlversuchen wird die Adresse für 15 Minuten gesperrt. Die Sperre wird pro E-Mail-Adresse in der Tabelle `login_lock` geführt, die bewusst keine Beziehung zu `app_user` hat. So greift die Sperre auch für Adressen ohne Konto und verrät nichts über deren Existenz.
5. Bei Erfolg wird der Zähler zurückgesetzt. Der Server antwortet mit 200, der `UserResponse` und einem neuen Cookie.

### Gültigkeit und Logout

Die Schnittstelle bietet keinen eigenen Endpunkt zum Erneuern des Tokens (Refresh). Nach Ablauf der Gültigkeit antwortet der nächste Request mit 401, und der Sportler meldet sich neu an. Eine Gültigkeit von acht Stunden deckt eine Trainingseinheit sicher ab.

`POST /api/auth/logout` antwortet mit 204 und einem Cookie mit leerem Wert und `Max-Age=0`, worauf der Browser das Cookie löscht. Da der Server zustandslos arbeitet, führt er keine Liste ausgestellter Tokens. Ein kopiertes Token bliebe darum bis zu seinem Ablauf gültig. Dieses Restrisiko ist durch `HttpOnly`, `Secure` und die begrenzte Gültigkeit klein. Eine Ausnahme bildet das Löschen des Kontos (`DELETE /api/users/me`): Weil der `JwtUserIdConverter` bei jedem Request prüft, ob der Sportler noch existiert, werden alle Tokens des gelöschten Kontos sofort ungültig. Auch dieser Endpunkt setzt das Logout-Cookie.

### Bestätigung durch das Passwort

Das Ändern der E-Mail-Adresse und des Passworts sowie das Löschen des Kontos verlangen zusätzlich das aktuelle Passwort im Body. Wer Zugriff auf eine offene Sitzung hat, etwa auf einem fremden Gerät, kann das Konto damit nicht übernehmen oder löschen. Ein falsches Passwort wird mit 401 beantwortet.

## OpenAPI-Spezifikation

Die OpenAPI-Spezifikation wird mit springdoc-openapi zur Laufzeit aus den Controllern und DTOs erzeugt. Sie stimmt damit immer mit der Implementierung überein und muss nicht von Hand nachgeführt werden. Die Spezifikation ist unter `/v3/api-docs` (JSON) bzw. `/v3/api-docs.yaml` abrufbar, die interaktive Oberfläche Swagger UI unter `/swagger-ui.html`.

Dieses Kapitel beschränkt sich auf die Übersicht. Die vollständige Spezifikation mit allen Feldern, Datentypen und Beispielen befindet sich im Anhang «OpenAPI-Spezifikation».
