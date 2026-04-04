# Stage 1: Build 
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Maven
COPY pom.xml .
COPY mvnw .
COPY .mvn/ .mvn/

RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -B
COPY src/ src/
RUN ./mvnw package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN mkdir -p /app/uploads
COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080
VOLUME ["/app/uploads"]
ENTRYPOINT ["java", "-jar", "/app/app.jar"]