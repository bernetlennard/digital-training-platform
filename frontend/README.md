# Digitale Plattform zur Trainingsverwaltung

Dieses Projekt ist eine digitale Plattform zur Trainingsverwaltung. Sie wurde im Rahmen einer Diplomarbeit an der TEKO Zürich von Lennard Bernet entwickelt.
Dieses Frontend ist die Weboberfläche der Plattform und ist für das Smartphone ausgelegt (Mobile-First).
Damit kann man Übungen und Routinen verwalten, Trainings planen, live protokollieren und den Verlauf ansehen. Die Daten kommen über die REST-Schnittstelle vom [Backend](../backend/README.md).

## Inhaltsverzeichnis

1. [Voraussetzungen](#1-voraussetzungen)
2. [Verwendete Technologien](#2-verwendete-technologien)
3. [Lokales Setup & Installation](#3-lokales-setup--installation)
   1. [Repository klonen](#schritt-1-repository-klonen)
   2. [Abhängigkeiten installieren](#schritt-2-abhängigkeiten-installieren)
   3. [Backend starten](#schritt-3-backend-starten)
   4. [Anwendung starten](#schritt-4-anwendung-starten)
   - [Weitere Befehle](#weitere-befehle)
4. [Technische Details](#4-technische-details)
   1. [Ordnerstruktur](#1-ordnerstruktur)
   2. [Vite-Proxy](#2-vite-proxy)
   3. [Material UI](#3-material-ui)
   4. [Fehlermeldungen](#4-fehlermeldungen)
   5. [Dockerfile](#5-dockerfile)

## 1. Voraussetzungen

Für den lokalen Betrieb muss folgende Software installiert sein:

- **Node.js 24.21.0:** Es muss genau diese Version sein (siehe [Kapitel 2](#2-verwendete-technologien)). npm ist bei Node.js dabei.
- **Git:** Zum Klonen des Repositorys.
- **Backend:** Das Backend muss lokal laufen, sonst kann man sich nicht anmelden. Die Anleitung dazu steht im [Backend-README](../backend/README.md).

Das Frontend braucht keine Umgebungsvariablen.

## 2. Verwendete Technologien

Dieses Frontend wurde mit folgenden Technologien entwickelt:

- **Node.js:** Version 24.21.0 (nur für die Entwicklung und den Build)
- **React:** Version 19.3.0
- **TypeScript:** Version 6.0.3
- **Vite:** Version 8.3.4 (Entwicklungsserver und Build)
- **Material UI:** Version 9.5.0 (Komponentenbibliothek)
- **React Router:** Version 8.4.0 (Seiten und Navigation)
- **React Hook Form:** Version 7.90.0 (Formulare)

Für die Prüfung und Formatierung des Codes:

- **ESLint:** Version 10.12.0
- **Prettier:** Version 3.9.10
- **Vitest:** Version 5.0.4

Alle Versionen stehen fix ohne `^` im `package.json`. Dafür sorgt `save-exact=true` im `.npmrc`. Die Versionen aller weiteren Abhängigkeiten sind im `package-lock.json` festgehalten.
Ebenfalls im `.npmrc` steht `engine-strict=true`. Damit bricht npm ab, wenn die installierte Node.js-Version nicht zur Angabe `engines` im `package.json` passt.

TypeScript läuft im «strict»-Modus. Dieser ist ab TypeScript 6 Standard und muss im `tsconfig` nicht extra eingeschaltet werden.

## 3. Lokales Setup & Installation

### Schritt 1: Repository klonen

Klonen Sie dieses Repository auf Ihren lokalen Computer:

```bash
git clone https://github.com/bernetlennard/digital-training-platform.git
cd digital-training-platform/frontend
```

### Schritt 2: Abhängigkeiten installieren

Installieren Sie im `/frontend`-Verzeichnis die Abhängigkeiten:

```bash
npm ci
```

`npm ci` installiert genau die Versionen aus dem `package-lock.json` in den Ordner `node_modules`.

### Schritt 3: Backend starten

Starten Sie zuerst das Backend gemäss [Backend-README](../backend/README.md).
Es muss unter http://localhost:8080 laufen, da das Frontend die Requests dorthin weiterleitet (siehe [Vite-Proxy](#2-vite-proxy)).

### Schritt 4: Anwendung starten

Die Anwendung kann in der lokalen Umgebung auf zwei Arten gestartet werden:

**Option A: Über die IDE (z.B. WebStorm)**
Öffnen Sie das `package.json` und starten Sie das Script `dev`.

**Option B: Über npm (Kommandozeile)**
Navigieren Sie im Terminal ins `/frontend`-Verzeichnis des Projekts und führen Sie folgenden Befehl aus:

```bash
npm run dev
```

Danach ist die Anwendung erreichbar unter http://localhost:5173. Ein Konto erstellt man direkt in der Anwendung über «Registrieren».
Änderungen am Code sind sofort im Browser sichtbar, ohne Neustart.

### Weitere Befehle

| Befehl              | Beschreibung                                           |
| ------------------- | ------------------------------------------------------ |
| `npm run lint`      | Prüft den Code mit ESLint                              |
| `npm run typecheck` | Prüft die Typen mit TypeScript                         |
| `npm test`          | Führt die Tests mit Vitest aus                         |
| `npm run build`     | Erstellt den Build für die Produktion im Ordner `dist` |
| `npm run format`    | Formatiert den Code mit Prettier                       |

Die ersten vier Befehle laufen auch in der GitHub-Pipeline bei jedem Pull Request auf `main`.

## 4. Technische Details

### 1. Ordnerstruktur

Der Code liegt im Ordner `src`:

| Ordner           | Inhalt                                                             |
| ---------------- | ------------------------------------------------------------------ |
| `api/`           | Requests an die REST-Schnittstelle, ein File pro Bereich           |
| `auth/`          | Angemeldeter Benutzer, Login und Logout                            |
| `components/`    | Bausteine, die auf mehreren Seiten verwendet werden, z.B. Dialoge  |
| `notifications/` | Kurze Meldungen am unteren Bildschirmrand, z.B. «Satz gespeichert» |
| `pages/`         | Ein File pro Seite, z.B. `LoginPage.tsx`                           |

`main.tsx` startet die Anwendung. In `App.tsx` steht, welche Seite unter welchem Pfad angezeigt wird.
Im Ordner `public` liegen die Icons und das Web-App-Manifest. Damit kann man die Anwendung auf dem Smartphone zum Home-Bildschirm hinzufügen.

### 2. Vite-Proxy

Lokal läuft das Frontend auf Port 5173 und das Backend auf Port 8080. Der Vite-Entwicklungsserver leitet alle Requests auf `/api` an das Backend weiter (siehe `vite.config.ts`).

Ohne diesen Proxy funktioniert der Login lokal nicht. Das Backend hat bewusst kein CORS, und das Cookie mit dem JWT ist HttpOnly und SameSite=Strict.
Mit dem Proxy laufen Frontend und API lokal unter derselben Adresse, genau wie später hinter Caddy auf dem Server.
Das Frontend kann das Token nicht lesen. Der Browser schickt das Cookie bei jedem Request selbst mit.

### 3. Material UI

Für das Aussehen wird die Komponentenbibliothek Material UI verwendet. Statt eigenem CSS kommen die Standardkomponenten wie Buttons, Dialoge und Listen zum Einsatz.
Einstellungen für alle Komponenten stehen zentral in `src/theme.ts`, z.B. deutsche Texte und mindestens 44 Pixel hohe Buttons für die Bedienung mit dem Finger.

Die Schrift Roboto wird über `@fontsource/roboto` mitgeliefert und nicht von Google Fonts geladen. So lädt der Browser nichts von Drittanbietern.

### 4. Fehlermeldungen

Alle Requests an das Backend laufen über `src/api/client.ts`.
Bei einem Fehler zeigt das Frontend einen deutschen Text passend zum HTTP-Statuscode an. Die englische Message vom Backend wird nie angezeigt.

Bei einem 401 ist die Sitzung abgelaufen und man wird automatisch abgemeldet.
Ausnahme sind der Login und Aktionen mit Passwortbestätigung (z.B. Konto löschen). Dort kann 401 auch «Passwort falsch» heissen.

### 5. Dockerfile

Für das Frontend wurde ein Dockerfile erstellt. Es besteht aus zwei Stufen:

1. **Build:** Ein Node-Image installiert die Abhängigkeiten und erstellt mit `npm run build` die statischen Dateien.
2. **Runtime:** nginx liefert diese Dateien aus. Das fertige Image enthält weder Node noch `node_modules`.

Die Konfiguration von nginx steht in `nginx.conf`. Pfade wie `/anmelden` gibt es nur im Browser.
Darum liefert nginx bei unbekannten Pfaden die `index.html` aus, und React Router zeigt die richtige Seite an.
Lint und Tests laufen nicht im Dockerfile, sondern vorher in der GitHub-Pipeline.
