# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------------
# Build: minimale Java-Runtime mit jlink und das Spring-Boot-JAR
# Basis-Images sind mit Tag und Digest fixiert (#NFA-7.13). Maven 3.9.16 und
# Temurin 25.0.4.1 entsprechen dem Maven-Wrapper und dem lokalen JDK.
# ---------------------------------------------------------------------------
FROM maven:3.9.16-eclipse-temurin-25-alpine@sha256:781f1da87cd3d142e66e47cc149c4582a652fb7f574648206d1e956170c0dc90 AS build
LABEL authors="Lennard Bernet"

# Nur die Module, die das Backend braucht, statt des ganzen JDK. Ermittelt mit
# jdeps --print-module-deps über die eigenen Klassen und alle Abhängigkeiten.
# Bei neuen Abhängigkeiten (z. B. Actuator) die Liste neu prüfen.
RUN jlink \
      --add-modules java.base,java.compiler,java.desktop,java.instrument,java.logging,java.management,java.naming,java.net.http,java.prefs,java.rmi,java.scripting,java.security.jgss,java.sql,java.sql.rowset,java.xml,jdk.jfr,jdk.unsupported \
      --strip-debug --no-man-pages --no-header-files --compress=zip-6 \
      --output /opt/jre

WORKDIR /build

# Abhängigkeiten in einer eigenen Schicht: Ändert sich nur der Code, kommen sie aus dem Cache
COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline

# Tests laufen vorher in der Pipeline (#NFA-7.22), hier wird nur gebaut
COPY src/main src/main
RUN mvn -B -ntp package -Dmaven.test.skip=true \
 && java -Djarmode=tools -jar target/*.jar extract --layers --launcher --destination extracted

# ---------------------------------------------------------------------------
# Runtime: nacktes Alpine mit der eigenen Runtime, ohne Maven und ohne JDK
# ---------------------------------------------------------------------------
FROM alpine:3.24.2@sha256:294b683cb724975bec92580e1e685676bd4b50bda910ddb8c51d4cabeaec77e6

ENV JAVA_HOME=/opt/java
ENV PATH="${JAVA_HOME}/bin:${PATH}"
COPY --from=build /opt/jre $JAVA_HOME

# Swagger UI und OpenAPI-Spezifikation sind im Container aus (#NFA-7.16), lokal bleiben sie an.
# Secrets (DB_PASSWORD, JWT_SECRET) kommen erst beim Start als Umgebungsvariablen, nie ins Image.
ENV SPRINGDOC_APIDOCS_ENABLED=false \
    SPRINGDOC_SWAGGERUI_ENABLED=false

# Ohne Root-Rechte ausführen
RUN addgroup -S app \
 && adduser -S -D -H -G app -s /sbin/nologin app
USER app

# Spring-Boot-Schichten von selten zu häufig geändert: Bei einem neuen Deployment
# ändert sich meist nur die letzte, kleine Schicht mit dem eigenen Code
WORKDIR /opt/app
COPY --from=build /build/extracted/dependencies/ ./
COPY --from=build /build/extracted/spring-boot-loader/ ./
COPY --from=build /build/extracted/snapshot-dependencies/ ./
COPY --from=build /build/extracted/application/ ./

EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
