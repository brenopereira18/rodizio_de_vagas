FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copia só o pom primeiro (cache de dependências)
COPY pom.xml .

RUN mvn dependency:go-offline

# Agora copia o resto do projeto
COPY src ./src

# Build da aplicação
RUN mvn clean package -DskipTests

# ===== RUNTIME STAGE =====
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]