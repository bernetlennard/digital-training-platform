# Digitale Plattform zur Trainingsverwaltung

Dieses Projekt ist eine digitale Plattform zur Trainingsverwaltung. Sie wurde im Rahmen einer Diplomarbeit an der TEKO Zürich von Lennard Bernet entwickelt.
Diese API ermöglicht das Erstellen, Löschen und Bearbeiten von Übungen, Routinen, Trainingsplanung, Live-Protokollierung, Historie und Benutzerverwaltung.

## Inhaltsverzeichnis

1. [Voraussetzungen](#1-voraussetzungen)
   * [Umgebungsvariablen](#umgebungsvariablen)
2. [Verwendete Technologien](#2-verwendete-technologien)
3. [Lokales Setup & Installation](#3-lokales-setup--installation)
   1. [Repository klonen](#schritt-1-repository-klonen)
   2. [Datenbank-Setup](#schritt-2-datenbank-setup)
   3. [Anwendung starten](#schritt-3-anwendung-starten)
4. [Technische Details](#4-technische-details)
   1. [Konfiguration](#1-konfiguration)
   2. [Dockerfile](#2-dockerfile)
   3. [Flyway-Migration](#3-flyway-migration)
   4. [Spring Security](#4-spring-security)
   5. [Transaction Handling](#5-transaction-handling)
   6. [Exception Handling](#6-exception-handling)
   7. [Swagger](#7-swagger)
   8. [API-Versionierung](#8-api-versionierung)

## 1. Voraussetzungen

Für den lokalen Betrieb muss folgende Software installiert sein:

* **JDK 25:** Die Umgebungsvariable `JAVA_HOME` muss auf das JDK zeigen, da der Maven Wrapper sie benötigt.
* **PostgreSQL 18:** Lokal laufender Datenbankserver.
* **Git:** Zum Klonen des Repositorys.

Maven muss nicht installiert sein. Der Maven Wrapper (`mvnw` bzw. unter Windows `mvnw.cmd`) lädt die passende Version automatisch herunter.

### Umgebungsvariablen

In `src/main/resources/application.properties` stehen nur Standardwerte. Diese werden über Umgebungsvariablen bzw. über ein `.env`-File im `/backend`-Verzeichnis überschrieben.
Die Properties müssen also nicht angepasst werden, es reicht, die Variablen zu setzen:

| Variable      | Pflicht | Standardwert                                                 | Beschreibung                                      |
|---------------|---------|--------------------------------------------------------------|---------------------------------------------------|
| `DB_PASSWORD` | Ja      | –                                                            | Passwort des Datenbankbenutzers                   |
| `JWT_SECRET`  | Ja      | –                                                            | Schlüssel zum Signieren der JWT, mind. 32 Zeichen |
| `DB_URL`      | Nein    | `jdbc:postgresql://localhost:5432/digital_training_platform` | JDBC-URL der Datenbank                            |
| `DB_USERNAME` | Nein    | `postgres`                                                   | Datenbankbenutzer                                 |

Fehlt eine Pflichtvariable oder ist das `JWT_SECRET` kürzer als 32 Zeichen, startet die Anwendung nicht.

Beispiel für ein `.env`-File:
```properties
DB_PASSWORD=mein-db-passwort
JWT_SECRET=ein-zufaelliger-schluessel-mit-mindestens-32-zeichen
```

## 2. Verwendete Technologien

Dieses Backend wurde mit folgenden Technologien entwickelt:

* **Java:** Version 25
* **PostgreSQL:** Version 18
* **Maven:** Version 3.9.16 (über den Maven Wrapper)
* **Spring Boot:** Version 4.1.1

Die meisten Abhängigkeiten werden ohne Versionsangabe im `pom.xml` eingebunden. Ihre Versionen gibt der `spring-boot-starter-parent` vor.
Dieser erbt von der BOM (Bill of Materials) `spring-boot-dependencies`, welche für alle unterstützten Bibliotheken eine getestete und zueinander passende Version festlegt.
Die folgenden Versionen werden durch Spring Boot 4.1.1 verwaltet:

* **Spring Framework:** Version 7.0.9
* **Spring Security:** Version 7.1.1
* **Spring Data JPA:** Release Train 2026.0.1
* **Hibernate:** Version 7.4.5.Final
* **Flyway:** Version 12.4.0
* **PostgreSQL JDBC-Treiber:** Version 42.7.13
* **JUnit Jupiter:** Version 6.0.3
* **Mockito:** Version 5.23.0
* **Lombok:** Version 1.18.46

Bibliotheken, welche die BOM nicht kennt, werden mit einer eigenen Version eingebunden:

* **springdoc-openapi:** Version 3.1.0
* **Bouncy Castle:** Version 1.86 (wird für das Argon2-Passwort-Hashing benötigt)

Die tatsächlich aufgelösten Versionen lassen sich mit `./mvnw dependency:tree` anzeigen.

## 3. Lokales Setup & Installation

### Schritt 1: Repository klonen
Klonen Sie dieses Repository auf Ihren lokalen Computer:
```bash
git clone https://github.com/bernetlennard/digital-training-platform.git
cd digital-training-platform/backend
```
### Schritt 2: Datenbank-Setup
Bevor die Anwendung gestartet werden kann, muss die PostgreSQL-Datenbank lokal eingerichtet werden.

**Schritt-für-Schritt-Einrichtung:**
1. Stellen Sie sicher, dass der PostgreSQL-Server läuft.
2. Erstellen Sie die Datenbank `digital_training_platform` auf dem PostgreSQL-Server.
3. Setzen Sie die Umgebungsvariablen gemäss [Kapitel 1](#umgebungsvariablen) im `.env`-File.
4. Dank der Flyway-Migration werden beim ersten erfolgreichen Start der Applikation alle Tabellen erstellt. Die Datenbank muss also leer sein.

### Schritt 3: Anwendung starten

Die Anwendung kann in der lokalen Umgebung auf zwei Arten gestartet werden:

**Option A: Über die IDE (z.B. IntelliJ IDEA)**
Führen Sie die Hauptklasse `src/main/java/ch/bernet/trainingplatform/TrainingplatformApplication.java` aus.

**Option B: Über Maven (Kommandozeile)**
Navigieren Sie im Terminal ins `/backend`-Verzeichnis des Projekts und führen Sie folgenden Befehl aus:
```bash
  ./mvnw spring-boot:run
```

## 4. Technische Details
### 1. Konfiguration
Die Konfigurationen werden nicht hardcodiert im Code hinterlegt. Dafür gibt es in Spring Boot ein sogenanntes Property File.
Darin stehen Merkmale wie die Datenbank-URL, JPA-Konfigurationen und das JWT-Secret.
Es gibt zusätzlich zu diesem File lokal noch ein `.env`-File. Hier werden Passwörter und Secrets abgelegt, welche nicht in das GitHub-Repository gepusht werden.
Das Property File verweist einfach auf diese Secrets.

### 2. Dockerfile
Für das Backend wurde ein Dockerfile erstellt.
Im Dockerfile wird Swagger ausgeschaltet, indem die Properties `SPRINGDOC_APIDOCS_ENABLED` und `SPRINGDOC_SWAGGERUI_ENABLED` auf `false` gesetzt werden.

### 3. Flyway-Migration
Beim Start der Spring-Boot-App schaut Flyway im Ordner «db/migration» nach SQL-Dateien.
Sie müssen dabei nach dem Schema «V1__...», «V2__...» benannt sein. Flyway führt jede Datei genau einmal aus, in der Reihenfolge der Versionsnummer.
Sobald eine Datei gelaufen ist, wird dies in der Tabelle «flyway_schema_history» gemerkt.
Anschliessend prüft Hibernate mit «ddl-auto=validate», ob die Entitäten zum Schema passen.

### 4. Spring Security
Mithilfe von Spring Security wird für die Benutzerauthentifizierung ein JWT erzeugt. Dieses wird beim Login als Cookie `access_token`
(HttpOnly, Secure, SameSite=Strict) gesetzt. Das JWT wird nur aus diesem Cookie gelesen, nicht aus dem `Authorization`-Header.
Das Token ist standardmässig 8 Stunden gültig (konfigurierbar über `app.jwt.validity`).

Die Anwendung ist stateless, es werden also keine Server-Sessions angelegt. Jeder Request wird allein anhand des Tokens authentifiziert.
Der CSRF-Schutz ist deaktiviert, da das Cookie mit SameSite=Strict bei Requests von fremden Seiten nicht mitgeschickt wird.

Passwörter werden nie im Klartext gespeichert, sondern mit Argon2id gehasht.

Folgende Endpunkte sind ohne Token erreichbar:

| Methode | Pfad                                                    |
|---------|---------------------------------------------------------|
| POST    | `/api/v1/auth/register`                                    |
| POST    | `/api/v1/auth/login`                                       |
| POST    | `/api/v1/auth/logout`                                      |
| *       | `/error`                                                |
| *       | `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**` |

Alle anderen Endpunkte brauchen ein gültiges JWT, ansonsten wird der Request mit 401 abgelehnt:

| Bereich          | Methode | Pfad                                                      |
|------------------|---------|-----------------------------------------------------------|
| Benutzer         | GET     | `/api/v1/users/me`                                           |
|                  | PUT     | `/api/v1/users/me`                                           |
|                  | PUT     | `/api/v1/users/me/email`                                     |
|                  | PUT     | `/api/v1/users/me/password`                                  |
|                  | DELETE  | `/api/v1/users/me`                                           |
| Übungen          | GET     | `/api/v1/exercises`                                          |
|                  | GET     | `/api/v1/exercises/muscle-groups`                            |
|                  | POST    | `/api/v1/exercises`                                          |
|                  | PUT     | `/api/v1/exercises/{exerciseId}`                             |
|                  | DELETE  | `/api/v1/exercises/{exerciseId}`                             |
| Routinen         | GET     | `/api/v1/routines`                                           |
|                  | GET     | `/api/v1/routines/{routineId}`                               |
|                  | POST    | `/api/v1/routines`                                           |
|                  | PUT     | `/api/v1/routines/{routineId}`                               |
|                  | DELETE  | `/api/v1/routines/{routineId}`                               |
| Trainingsplanung | GET     | `/api/v1/sessions/planned`                                   |
|                  | POST    | `/api/v1/sessions/planned`                                   |
|                  | PUT     | `/api/v1/sessions/planned/{sessionId}`                       |
|                  | DELETE  | `/api/v1/sessions/planned/{sessionId}`                       |
| Live-Training    | POST    | `/api/v1/sessions/planned/{sessionId}/start`                 |
|                  | POST    | `/api/v1/sessions`                                           |
|                  | GET     | `/api/v1/sessions/active`                                    |
|                  | PUT     | `/api/v1/sessions/active/note`                               |
|                  | POST    | `/api/v1/sessions/active/exercises/{sessionExerciseId}/sets` |
|                  | PUT     | `/api/v1/sessions/active/sets/{setId}`                       |
|                  | PUT     | `/api/v1/sessions/active/sets/{setId}/position`              |
|                  | DELETE  | `/api/v1/sessions/active/sets/{setId}`                       |
|                  | POST    | `/api/v1/sessions/active/finish`                             |
|                  | POST    | `/api/v1/sessions/active/cancel`                             |
| Historie         | GET     | `/api/v1/history/routines`                                   |
|                  | GET     | `/api/v1/history/routines/{routineId}/sessions`              |
|                  | GET     | `/api/v1/history/sessions/{sessionId}`                       |
|                  | DELETE  | `/api/v1/history/sessions/{sessionId}`                       |

### 5. Transaction Handling
Jegliche Service-Methoden enthalten jeweils die Annotation `@Transactional`, sobald eine Transaktion auf der Datenbank ausgeführt werden muss.
Über die Propagation kann man steuern, ob eine bestehende Transaktion übernommen wird, oder ob zwingend eine neue Transaktion erstellt werden soll.
Nach dem Ablauf der Methode wird die Transaktion auf der Datenbank committed.

### 6. Exception Handling
Jegliche Exceptions werden im Format des `ErrorResponse`-Records zurückgegeben.
Somit lassen sich übersichtliche und einheitliche Fehlermeldungen bilden. Dies stellt der `GlobalExceptionHandler` sicher.
Jegliche Messages, welche den Exceptions übergeben werden, sind als Konstanten definiert.
Somit lassen sich einheitliche Fehlermeldungen an einem gebündelten Ort im Code sauber verwalten.

### 7. Swagger
Swagger ist in der Anwendung integriert und liefert eine OpenAPI-Dokumentation. Es wird OpenAPI 3.1 verwendet.
In diesem Projekt wird jeder Controller mit einem Tag beschriftet.
Über die Klasse `ApiStatus` werden pro Endpunkt die HTTP-Statuscodes beschrieben.
Diese Dokumentation hilft dabei, die REST-API zu entwerfen, zu bauen, zu dokumentieren, zu testen und zu nutzen.

Swagger ist nur lokal aktiv, im Docker-Container ist es ausgeschaltet (siehe Dockerfile). Nach dem Start ist es erreichbar unter:

* **Swagger UI:** http://localhost:8080/swagger-ui.html
* **OpenAPI-Spezifikation:** http://localhost:8080/v3/api-docs

Die Swagger-Pfade sind in Spring Security freigegeben. Im Container liefern sie trotzdem 404, da Swagger dort ausgeschaltet ist.

Da das JWT nur aus dem Cookie gelesen wird, nützt der «Authorize»-Button von Swagger nichts.
Stattdessen in Swagger zuerst `POST /api/v1/auth/login` ausführen. Danach schickt der Browser das Cookie bei jedem Request automatisch mit.

### 8. API-Versionierung
Alle Endpunkte haben die Version im Pfad, z.B. `/api/v1/users/me`.

Es wird die eingebaute API-Versionierung von Spring Framework 7 verwendet. Die Klasse `ApiVersionConfig` schaltet sie ein.
Die Versionierung gilt nur für Pfade unter `/api/`. Swagger und `/error` haben keine Version.
Jeder Controller hat im `@RequestMapping` den Pfad `/api/v{version}/...` und die Version `1`, z.B. `@RequestMapping(path = "/api/v{version}/users/me", version = "1")`.

Somit erkennt man direkt am Pfad, welche Version die Schnittstelle verwendet.
Wenn man einen Endpunkt ändert, bricht man den Schnittstellenvertrag und das bestehende Frontend funktioniert nicht mehr.
Daher kommt die Änderung in eine neue Version `v2`. Die Version `v1` bleibt daneben bestehen. So kann man schrittweise umsteigen.
Eine Version, die es nicht gibt (z.B. `/api/v2/...`), wird mit 400 abgelehnt. 
Ein Pfad ohne Version (z.B. `/api/users/me`) ergibt 404.
