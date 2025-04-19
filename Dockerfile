# -------- BUILD STAGE --------
FROM gradle:jdk21-jammy AS build
WORKDIR /home/gradle/project

# Optional: ENV-Variablen vorbereiten (werden im Build aber nicht verwendet, nur Platzhalter)
ARG DB_URL
ARG DB_USER
ARG DB_PASSWORD

COPY --chown=gradle:gradle . .

RUN gradle build --no-daemon

# -------- RUNTIME STAGE --------
FROM eclipse-temurin:21-jdk-jammy

# Das erstellte .jar aus dem Build-Stage kopieren
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

# Falls du ENV-Variablen in der App verwendest, kannst du sie später mitgeben (z. B. per docker run)
ENTRYPOINT ["java", "-jar", "/app.jar"]
