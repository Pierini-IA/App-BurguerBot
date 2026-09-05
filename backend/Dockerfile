# Etapa 1: Build
# Usa la imagen oficial de Maven: el Maven wrapper (mvnw/.mvn) no está versionado
# en este repo, así que no se puede depender de él para el build de producción.
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Descargar dependencias primero (cache layer): solo se invalida si cambia el pom
COPY pom.xml ./
RUN mvn -B dependency:go-offline

# Copiar código fuente y compilar
COPY src ./src
RUN mvn -B clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar JAR desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto
EXPOSE 8080

# Variables de entorno por defecto (se pueden sobrescribir)
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xms512m -Xmx1024m"
# Zona horaria del mercado: sin esto los filtros "de hoy" (Pedidos, Dashboard)
# se rompen cerca de la medianoche UTC.
ENV TZ=America/Argentina/Buenos_Aires

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/health || exit 1

# Ejecutar aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
