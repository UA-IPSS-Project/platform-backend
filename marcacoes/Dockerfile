# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy parent POM first (needed to resolve the central POM)
COPY pom.xml .

# Copy module POM and download dependencies
COPY marcacoes/pom.xml marcacoes/
RUN mvn dependency:go-offline -B -pl marcacoes -am

# Copy source code and build
COPY marcacoes/src marcacoes/src
RUN mvn clean package -DskipTests -pl marcacoes -am

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/marcacoes/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
