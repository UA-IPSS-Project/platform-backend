# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy parent POM and module POM
COPY pom.xml .
COPY requisicoes/pom.xml requisicoes/

# Download dependencies
RUN mvn -f requisicoes/pom.xml dependency:go-offline -B

# Copy source and build
COPY requisicoes/src requisicoes/src
RUN mvn -f requisicoes/pom.xml clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/requisicoes/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]