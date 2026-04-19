# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos os pom.xml para satisfazer os módulos do projeto pai
COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/
COPY marcacoes/pom.xml marcacoes/
COPY requisicoes/pom.xml requisicoes/
COPY notificacoes/pom.xml notificacoes/
COPY common-data/pom.xml common-data/

# Baixar dependências (layer caching)
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -pl api-gateway -am -B

# Agora o código fonte
COPY api-gateway/src api-gateway/src
COPY common-data/src common-data/src

# Build
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -pl api-gateway -am -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/api-gateway/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]