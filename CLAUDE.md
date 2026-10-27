# Digitale Plattform zur Trainingsverwaltung – Projektübersicht

Sportler verwalten Übungen und Routinen, planen Trainings, protokollieren sie live (Sätze) und sehen den Verlauf.
Mobile-First-SPA (React/MUI) + REST-Backend (Spring Boot) + PostgreSQL, produktiv unter https://training.lennardbernet.ch hinter Caddy auf einem VPS.

Diese Datei und der Code sind die Referenz für Fachregeln, REST-Schnittstelle und Konventionen. Ändert sich eine Regel, wird sie hier nachgeführt.

## Repo-Layout

```
backend/    Spring Boot 4.1.1, Java 25, Maven-Wrapper (mvnw / mvnw.cmd), Dockerfile (jlink-JRE auf Alpine), README.md
  src/main/java/ch/bernet/trainingplatform/
    common/        ApiStatus (Swagger-Texte), ApiVersionConfig, exception/ (DomainException-Hierarchie, GlobalExceptionHandler, ErrorMessages)
    security/      SecurityConfig, JWT-Cookie (JwtCookieService, CookieBearerTokenResolver, JwtUserIdConverter), SecurityErrorHandler
    controller/{user,exercise,routine,training}/   dünne REST-Controller
    service/{user,exercise,routine,training}/      Logik + Mapper (statisch) + dto/ (Records mit Bean Validation)
    persistence/{user,exercise,routine,training}/  Entities + Repositories (JPQL-@Query mit ?1-Parametern)
  src/main/resources/application.properties, db/migration/ (Flyway, V1__init.sql)
  src/test/java/...          gleiche Paketstruktur, TestData.java
  src/test/rest/ST-0x-*.http Abläufe für den IntelliJ-HTTP-Client gegen das lokale Backend
frontend/   React 19.3 + TS 6 (strict) + Vite 8 + MUI 9.5 + React Router 8 + React Hook Form; Dockerfile (nginx-unprivileged), nginx.conf (CSP, SPA-Fallback), README.md
deploy/     docker-compose.yml (caddy, frontend, backend, db), Caddyfile, journald-retention.conf; Secrets in deploy/.env (nicht im Repo)
.github/workflows/  maven-tests.yml (Backend-Tests + Frontend lint/typecheck/test/build bei PR/Push auf main), deploy.yml (bei gemergtem PR auf main)
.claude/launch.json Preview-Configs «backend» (Port 8080) und «frontend» (Port 5173)
```

Branches: Arbeit auf `develop`, PR auf `main` deployt. Commits macht Lennard in der Regel selbst.

## Lokal bauen, starten, testen

- **Backend-Tests:** `cd backend && ./mvnw -q test`. `java`/`mvn` sind nicht im PATH; falls JAVA_HOME fehlt, `JAVA_HOME="$HOME/.jdks/temurin-25.0.4.1"` voranstellen.
- **Frontend:** `cd frontend && npm run lint && npm run typecheck && npm test && npm run build` (dieselben Schritte wie die Pipeline).
  - Vitest-Unit-Tests nur für reine Funktionen (`setForm`, `api/client`, `format`). Seiten und Komponenten werden von Hand im Browser getestet.
  - Zeitzone in `vite.config.ts` fix Europe/Zurich, Uhr per `vi.setSystemTime`.
- **App starten:** im Desktop-App-Browser per `preview_start` mit «backend» bzw. «frontend». Vite proxied `/api` auf 8080, damit das SameSite=Strict-Cookie ohne CORS funktioniert.
- **Backend-Konfiguration:** `backend/.env` (gitignored) mit `DB_PASSWORD`, `JWT_SECRET` (≥ 32 Zeichen, sonst Startabbruch); optional `DB_URL`, `DB_USERNAME`. Lokale DB: PostgreSQL 18 auf 5432, DB `digital_training_platform`.
- **Swagger** nur lokal: http://localhost:8080/swagger-ui.html (zuerst `POST /api/v1/auth/login`, der Authorize-Button nützt nichts). Im Container ist es aus.
- **Prettier-Falle:** `core.autocrlf=true`. `prettier --write` macht unveränderte Dateien zu «geändert» (CRLF→LF), diese mit `git checkout -- <datei>` zurücksetzen. `format:check` meldet einige nicht angefasste Dateien, die bleiben so. Nur geänderte Dateien formatieren (`npx prettier --write <datei>`).

## Backend

### Architektur-Muster
- Controller → Service → Repository. Benutzer-ID kommt als `@AuthenticationPrincipal Long userId` (JWT-subject). **Jede Query filtert nach userId**: fremde Daten ergeben 404, nie 403.
- Entities referenzieren andere Aggregate nur per ID (`userId`, `routineId`, `exerciseId` als `Long`), keine JPA-Beziehungen über Aggregatgrenzen. Innerhalb eines Aggregats: unidirektionales `@OneToMany(cascade=ALL, orphanRemoval=true) @JoinColumn @OrderBy`.
  - `Routine` → `RoutineExercise(position)`
  - `TrainingSession` → `SessionExercise(position)` → `ExerciseSet(setNumber)`
- Fachlogik steckt in den Entities (`TrainingSession.start/finish/revertToPlanned/reschedule/markDeleted`, `SessionExercise.addSet/updateSet/moveSet/removeSet`). Status-Guard verletzt → `NotFoundException` (404).
- Module sind über Interfaces entkoppelt:
  - `ExerciseUsageChecker` (Impl. `RoutineExerciseUsageChecker` in routine): Übung in aktiver Routine?
  - `RoutineDeletionListener` (Impl. `PlannedSessionCleaner` in training): löscht geplante Einheiten einer gelöschten Routine.
  - `SessionAccess` (training): Fassade auf RoutineService/ExerciseService (aktive Routine, Routinen-Summaries inkl. gelöschter, Übungsnamen inkl. gelöschter).
- Enums mit fester DB-ID über `AttributeConverter`: `MuscleGroup` 1..8 (Brust, Rücken, Schultern, Bizeps, Trizeps, Bauch, Beine, Gesäss), `SessionStatus` 1 PLANNED, 2 ACTIVE, 3 COMPLETED.
- `Clock`-Bean (Europe/Zurich) für alle Zeitpunkte; Tests nutzen `TestData.CLOCK` (fix 2026-10-15T08:00Z).
- Neue IDs: nach start/addSet `flush()`/`saveAndFlush()`, damit die Antwort die IDs enthält. Hartes Löschen mit `repository.delete(entity)`.
- Konstruktor-Injection ohne Lombok. Lombok nur im Paket `persistence` (Entities: `@Getter`, `@Setter`, `@NoArgsConstructor(access = PROTECTED)`, teils `@Builder`; Enums: `@Getter`).
- `@Transactional` auf den Service-Methoden, lesende mit `readOnly = true`.
- Controller: `@Tag(name = "…")` je Controller, pro Endpunkt `@Operation(summary = "…")` und je möglichem Status `@ApiResponse(responseCode, description = ApiStatus.X)`.

### Fehler → HTTP-Status (`ErrorResponse{status, message, details[]}`)
| Exception | Status | Typischer Fall |
|---|---|---|
| Bean Validation (`@Valid`) | 400 | details = `feld: meldung` |
| `AuthenticationFailedException` | 401 | falsches Passwort / unbekannte E-Mail (gleiche Antwort für beide) |
| `NotFoundException` | 404 | nicht vorhanden, fremd, falscher Status |
| `ConflictException` | 409 | Name/E-Mail vergeben, Übung in Routine, schon aktives Training, Abschluss ohne Satz |
| `BusinessRuleException` | 422 | Datum in Vergangenheit, doppelte Übung in Routine, Satzposition ausserhalb |
| `AccountLockedException` | 429 | Login gesperrt |
| sonstige | 500 | geloggt, ohne Details |

Messages sind englische Konstanten in `ErrorMessages` (keine String-Literale in `throw`); das Frontend zeigt sie nie an.

### Security
- JWT (HS256, `app.jwt.secret`, 8 h, `app.jwt.validity`) nur im Cookie `access_token` (HttpOnly, Secure, SameSite=Strict, Path=/). Kein Authorization-Header, stateless, CSRF aus.
- `CookieBearerTokenResolver` ignoriert das Cookie auf `/auth/register|login|logout` und `/api/actuator/health`, damit ein abgelaufenes Cookie nicht blockiert. Er prüft `getServletPath()`, das in MockMvc leer ist: Tests dafür brauchen `.servletPath(pfad)`.
- `JwtUserIdConverter`: Ist der Benutzer gelöscht, gibt es 401.
- Öffentlich: `POST /api/v1/auth/*`, `GET /api/actuator/health`, `/error`, Swagger-Pfade.
- Neues Cookie bei register, login, E-Mail- und Passwortänderung. Gelöscht wird es bei logout und Kontolöschung.
- Passwörter: Argon2id (`Argon2PasswordEncoder(16,32,1,60000,10)`, Bouncy Castle), 15–64 Zeichen.
- Login-Sperre (`LoginLockService`, Tabelle `login_lock` pro E-Mail, auch ohne Konto): ab 5 Fehlversuchen 15 min gesperrt (429). Fehlversuche zählen auch bei Passwortbestätigung (E-Mail/Passwort ändern, Konto löschen). Erfolg löscht den Eintrag; `registerFailure` läuft in `REQUIRES_NEW`. **Gewollt:** Nach Ablauf der Sperre zählt der Zähler weiter, der nächste Fehlversuch sperrt sofort wieder. Nicht als Bug melden.
- E-Mails werden getrimmt und kleingeschrieben (DB-Check `email = lower(email)`).
- API-Versionierung (Spring 7): `@RequestMapping(path="/api/v{version}/…", version="1")`. Unbekannte Version 400, ohne Version 404.

### Logging
- SLF4J (`LoggerFactory.getLogger`), Meldungen Englisch. Nur IDs (Benutzer, Entität, `login_lock`), nie E-Mail, Passwort oder Token.
- Jede schreibende Service-Methode loggt nach Erfolg auf `info`, z. B. `"Exercise {} created, user {}"`.
- `GlobalExceptionHandler`: `DomainException` auf `info` (Methode, Pfad, Status, Message, Benutzer), unerwartete Fehler auf `error` mit Stacktrace.
- Sicherheitsereignisse auf `warn`: `LoginLockService` (Fehlversuche, Sperren, abgewiesene Logins während der Sperre), `SecurityErrorHandler` (401/403), `JwtUserIdConverter` (Token eines gelöschten Benutzers).

### Datenmodell (Flyway, `db/migration`)
`app_user`, `login_lock`, `exercise`, `routine`, `routine_exercise`, `training_session`, `session_exercise`, `exercise_set`, Stammdaten `muscle_group`, `session_status`. Hibernate läuft mit `ddl-auto=validate`.
- **Soft Delete** (`deleted`):
  - `exercise` und `routine`: Name je Benutzer case-insensitive eindeutig, nur unter nicht gelöschten (Partial Unique Index).
  - `training_session`: nur bei Status 3.
- Pro Benutzer höchstens eine aktive Einheit (Partial Unique Index auf `status_id = 2`).
- `training_session(routine_id, user_id)` hat einen zusammengesetzten FK auf `routine(id, user_id)`.
- Positions- und Satznummer-Uniques sind `DEFERRABLE INITIALLY DEFERRED`, damit Umsortieren in einer Transaktion geht.
- Konto löschen = `ON DELETE CASCADE` ab `app_user` (FKs auf `exercise` deferred).
- Satzgrenzen: Gewicht 0–1000 kg (2 Nachkommastellen), Wiederholungen 1–100, Pause 0–3600 s oder null. Notiz ≤ 1000, Namen ≤ 100, Beschreibung ≤ 1000.
- **Bestehende Migrationen nie ändern:** Flyway bildet die Checksumme (CRC32) über alle Zeilen inkl. Kommentare (Zeilenenden und Leerzeilen zählen nicht). Jede Änderung ergibt einen «checksum mismatch», und das Backend startet auf einer bestehenden DB nicht mehr, bis `flyway_schema_history.checksum` angepasst ist. Schemaänderungen kommen in eine neue Datei `V2__….sql` usw.

### Fachlogik je Modul
- **Übungen** (`ExerciseService`):
  - Liste nur aktive Übungen. `findByIds` liefert auch gelöschte (für Namen in Verlauf/Training).
  - Löschen ergibt 409, wenn die Übung in einer nicht gelöschten Routine ist, sonst Soft Delete.
- **Routinen** (`RoutineService`):
  - Mindestens eine Übung, keine doppelte (422), alle müssen aktive eigene Übungen sein (404).
  - `replaceExercises` setzt die Positionen 1..n.
  - Löschen ist ein Soft Delete und löscht die geplanten Einheiten hart. Abgeschlossene bleiben im Verlauf.
- **Planung** (`TrainingPlanService`, `/sessions/planned`):
  - Datum nicht vor heute (422), Liste nach Datum sortiert.
  - Ändern nur für PLANNED und nur wenn altes und neues Datum ≥ heute. Vergangene geplante Einheiten lassen sich nur starten oder löschen.
- **Live-Training** (`LiveSessionService`, `/sessions`):
  - Start (geplant: 200, spontan: 201 mit `plannedDate = null`). Ist schon ein Training aktiv, gibt es 409. Die Übungsliste der Routine wird beim Start kopiert, spätere Änderungen an der Routine wirken nicht auf das laufende Training.
  - `GET /sessions/active` liefert 200 oder 204.
  - Satz-Endpunkte antworten mit der ganzen `SessionExerciseResponse` (alle Sätze + Vorschläge). Satznummern sind immer lückenlos 1..n, auch nach Verschieben und Löschen.
  - Abschliessen braucht mindestens einen Satz (sonst 409). Übungen ohne Satz werden dabei entfernt.
  - Abbrechen: Eine geplante Einheit geht zurück auf PLANNED (Übungen weg), eine spontane wird hart gelöscht. Wurde die Routine während des Trainings gelöscht, wird auch die geplante hart gelöscht (sonst bliebe eine nicht startbare geplante Einheit).
  - **Satzvorschläge** (`suggestOpenSets` + `ExerciseSetRepository.findLastCompletedSets`):
    - Quelle: die Sätze der letzten abgeschlossenen, nicht gelöschten Einheit **derselben Routine**, in der die Übung vorkam. Wurde die Übung dort ausgelassen, wird weiter zurückgeschaut.
    - `suggestedSets` sind die Sätze mit `setNumber` > Anzahl bereits erfasster Sätze. Sie sind nicht gespeichert.
    - Danach oder ohne Historie bleibt die Liste leer. Der Verlauf liefert immer `[]`.
- **Verlauf** (`HistoryService`, `/history`):
  - Routinen mit mindestens einer abgeschlossenen Einheit, auch gelöschte Routinen mit `deleted`-Flag.
  - Einheiten neueste zuerst, Detail nur für abgeschlossene, nicht gelöschte Einheiten.
  - Löschen ist ein Soft Delete.
- **Benutzer** (`UserService`, `/auth`, `/users/me`):
  - Registrierung nur mit E-Mail und Passwort; das Frontend setzt Vor- und Nachname danach per `PUT /users/me`.
  - E-Mail ändern, Passwort ändern und Konto löschen verlangen das Passwort (401 bei falschem).

### Endpunkte (`/api/v1`, Status bei Erfolg)
- **auth:** `POST register` 201, `POST login` 200, `POST logout` 204
- **users/me:** `GET` 200, `PUT` 200 (Profil), `PUT /email` 200, `PUT /password` 204, `DELETE` 204 (Body mit Passwort)
- **exercises:** `GET` 200, `GET /muscle-groups` 200, `POST` 201, `PUT /{id}` 200, `DELETE /{id}` 204
- **routines:** `GET` 200, `GET /{id}` 200, `POST` 201, `PUT /{id}` 200, `DELETE /{id}` 204
- **sessions/planned:** `GET` 200, `POST` 201, `PUT /{id}` 200, `DELETE /{id}` 204, `POST /{id}/start` 200 (liegt im `LiveSessionController`)
- **sessions:**
  - `POST` 201 (spontan), `GET /active` 200/204
  - `PUT /active/note` 204
  - `POST /active/exercises/{sessionExerciseId}/sets` 201
  - `PUT /active/sets/{setId}` 200, `PUT /active/sets/{setId}/position` 200, `DELETE /active/sets/{setId}` 200
  - `POST /active/finish` 204, `POST /active/cancel` 204
- **history:** `GET /routines` 200, `GET /routines/{id}/sessions` 200, `GET /sessions/{id}` 200, `DELETE /sessions/{id}` 204
- **Health-Check (Actuator, ohne Version):** `GET /api/actuator/health` 200 `{"status":"UP"}` bzw. 503 (prüft auch die DB), nie Details. Er liegt unter `/api`, weil Caddy nur `/api/*` ans Backend gibt. Alle anderen Actuator-Pfade brauchen ein Token. Im `@WebMvcTest` fehlt Actuator, darum importiert `SecurityConfigTest` die Autokonfigurationen per `@ImportAutoConfiguration`.
- Bei neuen oder geänderten Endpunkten die Endpunkt-Tabelle in `backend/README.md` nachführen, bei DTO-Änderungen `frontend/src/api/types.ts`.

### Tests (TDD)
- Ablauf: Test zuerst schreiben und rot sehen, dann implementieren. `@DisplayName` beginnt mit der UT-Nummer des Themas (Tabelle unten). Eine UT-Nummer verteilt sich auf mehrere Klassen.
- Testarten:
  - Service-Tests: Mockito, strict stubs. Ein unnötiger oder abweichender Stub schlägt fehl.
  - Controller: `@WebMvcTest(X.class)` mit `@Import({SecurityConfig, SecurityErrorHandler, CookieBearerTokenResolver, JwtCookieService})` und `@MockitoBean`.
  - Ausserdem `RequestValidationTest`, `SecurityConfigTest` und Entity-Tests.
- `TestData`: `USER_A/B`, `BENCH_PRESS/SQUAT/PULL_UP`, `ROUTINE_ID`, `SESSION_ID`. `withId(entity, id)` simuliert DB-IDs.
- Test-Properties: `src/test/resources/config/application.properties` (Dummy-JWT-Secret).
- UT-Zuordnung:

| UT | Thema | UT | Thema |
|---|---|---|---|
| 01 | Fehlerformat, Validierung | 09 | Routine löschen (soft, Planung weg) |
| 02 | 401 ohne Token, fremde Daten 404 | 10 | Training planen |
| 03 | Registrierung | 11 | Training starten (geplant/spontan) |
| 04 | Login, Login-Sperre | 12 | Sätze erfassen/ändern/verschieben/löschen |
| 05 | Profil, E-Mail, Passwort, Logout, Konto löschen | 13 | Satzvorschläge |
| 06 | Übungen CRUD | 14 | Notiz, Abschliessen, Abbrechen |
| 07 | Übung löschen (409/soft) | 15 | Verlauf |
| 08 | Routinen CRUD | | |

- Frontend-Tests heissen `UTF-xx` (`describe('UTF-01 …')`): 01/02 `setForm`, 03–05 `api/client`, 06 `format`.

## Frontend (`frontend/src`)

- `main.tsx`: ThemeProvider (deDE-Locale, Touch-Ziele ≥ 44 px) → BrowserRouter → NotificationProvider → AuthProvider → `App.tsx` (Routen). Roboto lokal über @fontsource; der Browser lädt nichts von Drittanbietern.
- **Routen:**
  - Öffentlich: `/anmelden`, `/registrieren` (`RedirectIfAuthenticated`), `/datenschutz` und `/anleitung` (immer, beide im Layout `InfoPage`).
  - Mit Anmeldung (`RequireAuth`) in `AppLayout`: `/` StartPage, `/routinen`, `/routinen/:routineId` (`neu` = neue Routine), `/uebungen`, `/verlauf`, `/verlauf/:routineId`, `/verlauf/einheit/:sessionId`, `/konto`.
  - `/training`: LiveTrainingPage im Vollbild, ohne Navigation.
  - Navigation: unten auf dem Smartphone, Seitenleiste ab Tablet. Kopfzeile rechts: Fragezeichen → `/anleitung`, Person → `/konto`.
  - Anleitung: Der Text steht in `SECTIONS` (`GuidePage.tsx`). Ändert sich ein Ablauf in der App, die Anleitung mitprüfen.
- **`api/`**:
  - `client.ts` `request<T>(path, {method, body, checksPassword})` → `fetch('/api/v1'+path)`, wirft `ApiError(status, message, details)`. Status 0 heisst Server nicht erreichbar.
  - Ein 401 ruft den globalen Unauthorized-Handler (Logout), ausser bei `/auth/*` oder `checksPassword` (dort heisst 401 «Passwort falsch»).
  - `getErrorMessage(error, byStatus)` liefert deutsche Texte je Statuscode, nie die Backend-Message.
  - `parseId(param)` wirft bei ungültiger ID einen 404.
  - Module je Bereich: `auth`, `users`, `exercises`, `routines`, `sessions` (Planung + Live), `history`. `types.ts` spiegelt die Backend-DTOs (`IsoDate`, `IsoInstant`).
  - `useApiData(load)`: `{data, error, isLoading, reload, setData}`. `load` muss stabil sein (Modulfunktion oder useCallback). `setData` übernimmt Serverantworten direkt, ohne neu zu laden.
- **`auth/`**: `AuthProvider` prüft beim Start `GET /users/me`. `useAuth()` liefert `user`, `login`, `register`, `logout`, `setUser`. Das Token ist für JS unsichtbar.
- **`components/`**:
  - `AsyncContent` (Spinner, Fehler mit «Erneut versuchen»), `FormDialog`, `ConfirmDialog` (Fehler bleiben im Dialog), `PageHeader`, `EmptyState`.
  - `ExerciseDialog`, `SessionDialogs` (Planen/Ändern, spontan starten), `AccountDialogs`, `SessionExerciseCard` (Live-Übung), `SetFields` + `setForm.ts`.
  - `formFields.ts`: `textFieldProps(register(...))` verbindet RHF mit MUI (`inputRef`). Dazu Validierungsregeln (`emailRules`, `newPasswordRules`, `currentPasswordRules`, `nameRules`, `personNameRules`, `longTextRules` ≤ 1000), die nur der Führung dienen; verbindlich prüft das Backend.
- **Muster:**
  - Formulare mit React Hook Form. Satzwerte sind Strings, Komma oder Punkt erlaubt.
  - Nach dem Speichern die Serverantwort übernehmen und `notify('…')` aufrufen (Snackbar).
  - Bei Fehlern bleiben die Eingaben stehen.
  - Texte sind Deutsch (Schweiz, «ss»). Formate in `format.ts` (de-CH, `formatSet`, `formatWeekdayDate` «Di., 20.10.2026», `formatStart` «um 18:30» bzw. «am …», `formatExerciseList` «2 Übungen: A, B», `countLabel`, `byName`).
  - Buttons ohne Grossbuchstaben (`theme.ts`), eine hervorgehobene Hauptaktion pro Ansicht.
  - Snackbar: `AppLayout` setzt auf dem Handy die CSS-Variable `--bottom-nav-height`, die Snackbar steht darüber. Ohne Navigation (Live-Training) liegt sie am unteren Rand.
  - Lint-Regel `react-hooks/refs`: keine `ref.current` im Render. Für «geändert?» `getFieldState(name).isDirty` von RHF nutzen.
- **Live-Training:**
  - `LiveTrainingPage` lädt immer `GET /sessions/active`, damit ein Training nach einem Reload weiterläuft. Kopfzeile: Startzeit und laufende Dauer (`StartedInfo`, alle 30 s).
  - Übungsbeschreibungen lädt sie getrennt über `GET /exercises` (die Einheit liefert nur Namen). `ExerciseHint` zeigt sie oben in der Karte, ab 160 Zeichen oder mehr als 3 Zeilen gekürzt mit «Mehr anzeigen». Die Übungsliste zeigt sie unter der Muskelgruppe, auf 2 Zeilen gekürzt.
  - `SessionExerciseCard`:
    - Das `NewSetForm` ist mit `suggestedSets[0]` vorbelegt und wird über `key={sets.length}` neu aufgebaut. Die übrigen Vorschläge stehen als «Satz n · offen» darunter.
    - Fortschritt «x von y Sätzen», grünes Häkchen, sobald keine Vorschläge mehr offen sind.
    - Antippen eines erfassten Satzes öffnet `EditSetDialog`: Werte (PUT), Position (PUT /position), «Löschen» mit Rückfrage.
    - Accordion-Überschrift ist `h2` (`slotProps.heading`), der Name darin ein `span`.
  - `SetFields` markiert beim Fokus den Wert, damit eine neue Zahl ihn ersetzt.
  - Notiz speichert sich beim Verlassen des Felds (`onBlur`), Abschliessen speichert eine ungespeicherte Notiz mit.
- **Start:**
  - Laufendes Training zuoberst.
  - Geplante Trainings mit Wochentag; nur das nächste hat einen gefüllten «Starten»-Knopf (und nur, wenn kein Training läuft).
  - «Spontan starten» zeigt die Routinen als Liste, ein Tipp startet.
- **Routine bearbeiten:** Der Übungs-Picker erlaubt Mehrfachauswahl in Tipp-Reihenfolge, ab 8 Übungen mit Suchfeld.
- **PWA-light:** `public/manifest.webmanifest`, `icon-192/512.png`, `apple-touch-icon.png`, `favicon.svg` (Hantel weiss auf #1976d2), `theme-color`. Ohne Service Worker. nginx liefert das Manifest als `application/manifest+json`.

## Deploy & CI
- `deploy.yml` läuft bei gemergtem PR auf main:
  - baut beide Images und pusht sie nach Docker Hub (`bernetlennard/dtp-{backend,frontend}:<merge-sha>` und `:latest`).
  - startet per SSH auf dem VPS `docker compose pull && up -d --wait` in `/home/ubuntu/trainingplatform` (mit `IMAGE_TAG=<merge-sha>`).
  - ruft danach `https://training.lennardbernet.ch/api/actuator/health` mit curl auf (Retries bis ca. 2 min). Bei einer Antwort ungleich 2xx wird der Job rot.
- Secrets: GitHub `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`, `VPS_SSH_KEY`, `VPS_KNOWN_HOSTS`, `VPS_USER`, `VPS_HOST`. Auf dem VPS `deploy/.env` mit `DOMAIN`, `DB_PASSWORD`, `JWT_SECRET`.
- `POSTGRES_PASSWORD` gilt nur beim ersten Start mit leerem Volume. Ein neues `DB_PASSWORD` muss zusätzlich in PostgreSQL gesetzt werden (`ALTER USER postgres …`).
- Der Workflow kopiert keine Dateien auf den VPS. Änderungen an `docker-compose.yml`, `Caddyfile` und `journald-retention.conf` kopiert Lennard von Hand nach `/home/ubuntu/trainingplatform` (bzw. `/etc/systemd/journald.conf.d/`).
- **Logs:** alle Container mit `logging: driver: journald`. Die Aufbewahrung (höchstens 14 Tage, Dateien täglich rotiert) regelt `deploy/journald-retention.conf` auf dem Host. Lesen weiterhin mit `docker compose logs`.
- **Caddy:** TLS über Let's Encrypt, HSTS. `/api/*` geht ans Backend, alles andere ans Frontend (nginx :8080). Nur h1/h2 (die Firewall lässt kein UDP durch). Netz `db` ist intern.
- **Images:** alle Basis-Images mit Tag und Digest fixiert. npm `save-exact`, `engine-strict` (Node 24.21.0). Swagger im Backend-Image aus. Die jlink-Module im Backend-Dockerfile kommen aus `jdeps --print-module-deps`. `jdk.management` braucht Micrometer (Actuator), sonst warnt `JvmGcMetrics` bei jedem Start.

## Konventionen
- Code-Bezeichner Englisch, Kommentare, UI- und Swagger-Texte Deutsch. Log- und Fehlermeldungen (`ErrorMessages`) Englisch.
- **Keine Verweise auf externe Dokumente, Anforderungs- oder Kapitelnummern**, weder im Code noch in Kommentaren, READMEs oder dieser Datei. Testnummern (`UT-xx`, `UTF-xx`, `ST-xx`) stehen nur in Testnamen und den `.http`-Dateien.
- **Kommentare sparsam** (Wunsch Lennard): nur wo etwas nicht offensichtlich ist (Fallen, Begründungen, magische Werte). Nichts, was der Code oder Name schon sagt.
  - Backend: Die Einzeiler am Anfang jeder Methode bleiben (Lennard will sie behalten), JPQL-Hinweise über Repository-Methoden ebenso. Neue Methoden bekommen denselben kurzen Einzeiler.
  - Frontend: keine JSDoc-Zeilen, die nur den Namen wiederholen.
  - Dockerfiles, Compose, Caddyfile, nginx, Workflows, Properties, SQL-Migrationen: praktisch ohne Kommentare.
- Prettier: singleQuote, no semi. ESLint mit react-hooks.
- Alle Versionen fix, keine CDNs, keine CORS (gleiche Domain). Bei Versionswechseln die Versionsangaben in den READMEs und hier nachführen.
- Diese Datei bleibt stabil: nur dauerhaft gültige Fakten und Regeln, keine Daten, Zählungen oder Zwischenstände.
