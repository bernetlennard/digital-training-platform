# syntax=docker/dockerfile:1

FROM node:24.21.0-alpine3.24@sha256:ebfe2f90462722a7a4de65e91990e97fe0d401c70e0e762c5b53302f905ec1c1 AS build
LABEL authors="Lennard Bernet"

WORKDIR /build

COPY package.json package-lock.json .npmrc ./
RUN npm ci

COPY index.html tsconfig.json tsconfig.app.json tsconfig.node.json vite.config.ts ./
COPY public public
COPY src src
RUN npm run build

FROM nginxinc/nginx-unprivileged:1.30.5-alpine-slim@sha256:7f79f463c6eb0d0bf8782992bb9636434398e3e5848398805aef2c9b025d8609

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /build/dist /usr/share/nginx/frontend

EXPOSE 8080
