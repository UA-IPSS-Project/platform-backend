# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy parent and module poms so Maven can resolve the multi-module structure
COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/
COPY marcacoes/pom.xml marcacoes/
COPY requisicoes/pom.xml requisicoes/
COPY candidaturas/pom.xml candidaturas/

# Download dependencies
RUN --mount=type=cache,target=/root/.m2 \
    mvn -f candidaturas/pom.xml dependency:go-offline -B

# Copy source and build
COPY candidaturas/src candidaturas/src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -f candidaturas/pom.xml clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/candidaturas/target/*.jar app.jar

# Expose service port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
