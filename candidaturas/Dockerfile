# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy parent and module poms so Maven can resolve the multi-module structure
COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/
COPY marcacoes/pom.xml marcacoes/
COPY requisicoes/pom.xml requisicoes/
COPY notificacoes/pom.xml notificacoes/
COPY candidaturas/pom.xml candidaturas/
COPY common-data/pom.xml common-data/

# Download dependencies
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -pl candidaturas -am -B

# Copy source code and build
COPY candidaturas/src candidaturas/src
COPY common-data/src common-data/src
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -pl candidaturas -am -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/candidaturas/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
