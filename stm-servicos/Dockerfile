# ===============================
# Stage 1: Build
# ===============================
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Copia os arquivos do projeto
COPY . .

# Permite execução do Gradle Wrapper
RUN chmod +x ./gradlew

# Build da aplicação usando Gradle
RUN ./gradlew clean bootJar --no-daemon

# ===============================
# Stage 2: Runtime
# ===============================
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copia o JAR gerado do stage de build
COPY --from=build /app/build/libs/*.jar app.jar

# Porta que a aplicação vai expor
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
