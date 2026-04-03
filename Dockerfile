# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and configuration first
COPY pom.xml .
COPY mvnw .
COPY .mvn/ .mvn/

RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src/ src/

# Build the application (skip tests)
RUN ./mvnw package -DskipTests -B

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create directory for runtime uploads
RUN mkdir -p /app/uploads

# Copy built JAR from build stage
COPY --from=build /app/target/*.jar /app/app.jar

# Expose application port
EXPOSE 8080

# Optional: define uploads as a volume
VOLUME ["/app/uploads"]

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]