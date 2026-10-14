# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------------
# Build: statische Dateien mit Vite erzeugen
# Basis-Images sind mit Tag und Digest fixiert (#NFA-7.13).
# ---------------------------------------------------------------------------
FROM node:24.21.0-alpine3.24@sha256:ebfe2f90462722a7a4de65e91990e97fe0d401c70e0e762c5b53302f905ec1c1 AS build
LABEL authors="Lennard Bernet"

WORKDIR /build

# Abhängigkeiten in einer eigenen Schicht: Ändert sich nur der Code, kommen sie aus dem Cache.
# .npmrc mit engine-strict: npm ci bricht ab, wenn die Node-Version nicht zu package.json passt
COPY package.json package-lock.json .npmrc ./
RUN npm ci

# Lint und Tests laufen vorher in der Pipeline (#NFA-7.22), hier wird nur gebaut
COPY index.html tsconfig.json tsconfig.app.json tsconfig.node.json vite.config.ts ./
COPY public public
COPY src src
RUN npm run build

# ---------------------------------------------------------------------------
# Runtime: nginx liefert nur die statischen Dateien aus (E3), die SPA läuft im Browser.
# Ohne Node und ohne node_modules. Die unprivileged-Variante läuft ohne
# Root-Rechte auf Port 8080, die slim-Variante enthält nur nginx selbst.
# ---------------------------------------------------------------------------
FROM nginxinc/nginx-unprivileged:1.30.5-alpine-slim@sha256:7f79f463c6eb0d0bf8782992bb9636434398e3e5848398805aef2c9b025d8609

COPY nginx.conf /etc/nginx/conf.d/default.conf

# Eigenes Verzeichnis, damit die Standardseiten von nginx nicht mit ausgeliefert werden
COPY --from=build /build/dist /usr/share/nginx/frontend

EXPOSE 8080
