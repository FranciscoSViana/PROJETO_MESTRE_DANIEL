# ===============================
# Stage 1: Build
# ===============================
FROM ubuntu:latest AS build

# Atualiza pacotes e instala Java 21
RUN apt-get update && apt-get install -y openjdk-21-jdk unzip curl

# Define diretório de trabalho
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
FROM openjdk:21-jdk-slim

WORKDIR /app

# Copia o JAR gerado do stage de build
COPY --from=build /app/build/libs/*.jar app.jar

# Porta que a aplicação vai expor
EXPOSE 8080

# Comando para iniciar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
