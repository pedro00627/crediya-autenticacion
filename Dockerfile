# === Etapa 1: Construcción (Build Stage) ===
FROM gradle:9.0.0-jdk21-jammy
WORKDIR /home/gradle/src

# Copiar archivos esenciales para el build
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Asegurar permisos de ejecución para gradlew
RUN chmod +x gradlew

# Copiar el código fuente
COPY src ./src

# Construir el JAR sin ejecutar los tests
RUN ./gradlew bootJar --no-daemon -x test

# === Etapa 2: Imagen final (Runtime Stage) ===
FROM gcr.io/distroless/java21-debian12
WORKDIR /app

# Copiar el JAR generado desde la etapa de construcción
COPY --from=build /home/gradle/src/build/libs/*.jar ./app.jar

# Exponer el puerto de la aplicación
EXPOSE 8080

# Comando de inicio
ENTRYPOINT ["java", "-jar", "Autenticacion.jar"]