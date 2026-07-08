# Stage 1: Build the Angular Web App
FROM docker.io/library/node:22-alpine AS frontend-builder
WORKDIR /app/angular-app
COPY angular-app/package*.json ./
RUN npm ci
COPY angular-app/ ./
RUN npm run build

# Stage 2: Build the Spring Boot App
FROM docker.io/library/eclipse-temurin:21-jdk AS backend-builder
WORKDIR /app
# Copy gradle files
COPY backend/gradlew /app/backend/
COPY backend/gradle /app/backend/gradle
COPY backend/settings.gradle.kts /app/backend/
COPY backend/gradle.properties /app/backend/
COPY backend/app/build.gradle.kts /app/backend/app/

# Build caching step: download gradle dependencies
WORKDIR /app/backend
RUN ./gradlew dependencies --no-daemon || true

# Copy all source files
WORKDIR /app
COPY backend /app/backend

# Create static resources directory in case it doesn't exist
RUN mkdir -p /app/backend/app/src/main/resources/static

# Copy the built Angular app into Spring Boot static resources
COPY --from=frontend-builder /app/angular-app/dist/livebus-web/browser/ /app/backend/app/src/main/resources/static/

# Build the Spring Boot application
WORKDIR /app/backend
RUN ./gradlew :app:bootJar --no-daemon

# Stage 3: Runtime
FROM docker.io/library/eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-builder /app/backend/app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
