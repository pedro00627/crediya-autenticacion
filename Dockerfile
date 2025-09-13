# === Etapa 1: Construcción (Build Stage) ===
FROM gradle:8.5.0-jdk17-jammy AS build
WORKDIR /home/gradle/src

COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

RUN ./gradlew build --no-daemon -x test || true

COPY src ./src

RUN ./gradlew bootJar --no-daemon -x test

# === Etapa 2: Ejecución (Runtime Stage) ===
FROM gcr.io/distroless/java17-debian11
WORKDIR /app

# Asumimos que el JAR generado se llama 'Autenticacion.jar' y está en una ruta similar
COPY --from=build /home/gradle/src/applications/app-service/build/libs/Autenticacion.jar .

# Exponemos el puerto 8081, que es el que definimos en docker-compose.yml
EXPOSE 8081

# El comando de inicio usa el nombre del JAR de este microservicio
ENTRYPOINT ["java", "-jar", "Autenticacion.jar"]