# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos os pom.xml para satisfazer os módulos do projeto pai
COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/
COPY marcacoes/pom.xml marcacoes/
COPY requisicoes/pom.xml requisicoes/

# Download dependencies
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B -f marcacoes/pom.xml

# Copy source code and build
COPY marcacoes/src marcacoes/src
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -f marcacoes/pom.xml

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/marcacoes/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
