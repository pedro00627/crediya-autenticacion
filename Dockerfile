# === Stage 1: Build microservice ===
FROM gradle:9.0.0-jdk21-alpine AS build
WORKDIR /workspace

# Copy only the microservice files
COPY Autenticacion ./

# Set GitHub Packages credentials
ARG GITHUB_ACTOR
ARG GITHUB_TOKEN
ENV GITHUB_ACTOR=${GITHUB_ACTOR}
ENV GITHUB_TOKEN=${GITHUB_TOKEN}

# Build microservice JAR (will download Common from GitHub Packages)
RUN cd . && \
    ./gradlew bootJar --no-daemon --quiet --parallel -x test && \
    find . -name "*.jar" -not -path "*/build/libs/*" -delete

# === Stage 3: Ultra-minimal runtime ===
FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /app

# Copy only the final JAR
COPY --from=build /workspace/applications/app-service/build/libs/*.jar ./autenticacion.jar

# Use non-root user for security
USER nonroot:nonroot

# Optimized JVM settings for containers
EXPOSE 8080
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-XX:+UseStringDeduplication", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "/app/autenticacion.jar"]