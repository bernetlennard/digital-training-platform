# syntax=docker/dockerfile:1

FROM maven:3.9.16-eclipse-temurin-25-alpine@sha256:781f1da87cd3d142e66e47cc149c4582a652fb7f574648206d1e956170c0dc90 AS build
LABEL authors="Lennard Bernet"

# Module ermittelt mit jdeps --print-module-deps, bei neuen Abhängigkeiten neu prüfen
RUN jlink \
      --add-modules java.base,java.compiler,java.desktop,java.instrument,java.logging,java.management,java.naming,java.net.http,java.prefs,java.rmi,java.scripting,java.security.jgss,java.sql,java.sql.rowset,java.xml,jdk.jfr,jdk.management,jdk.unsupported \
      --strip-debug --no-man-pages --no-header-files --compress=zip-6 \
      --output /opt/jre

WORKDIR /build

COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline

COPY src/main src/main
RUN mvn -B -ntp package -Dmaven.test.skip=true \
 && java -Djarmode=tools -jar target/*.jar extract --layers --launcher --destination extracted

FROM alpine:3.24.2@sha256:294b683cb724975bec92580e1e685676bd4b50bda910ddb8c51d4cabeaec77e6

ENV JAVA_HOME=/opt/java
ENV PATH="${JAVA_HOME}/bin:${PATH}"
COPY --from=build /opt/jre $JAVA_HOME

ENV SPRINGDOC_APIDOCS_ENABLED=false \
    SPRINGDOC_SWAGGERUI_ENABLED=false

RUN addgroup -S app \
 && adduser -S -D -H -G app -s /sbin/nologin app
USER app

WORKDIR /opt/app
COPY --from=build /build/extracted/dependencies/ ./
COPY --from=build /build/extracted/spring-boot-loader/ ./
COPY --from=build /build/extracted/snapshot-dependencies/ ./
COPY --from=build /build/extracted/application/ ./

EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
