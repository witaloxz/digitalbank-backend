# Dockerfile
# Estágio 1: Build
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copiar apenas pom.xml primeiro (cache de dependências)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fonte
COPY src ./src
RUN mvn clean package -DskipTests -B

# Estágio 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Instalar ferramentas úteis
RUN apk add --no-cache curl

# Criar usuário não-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar JAR
COPY --from=builder /app/target/*.jar app.jar

# JVM otimizada para container
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]