# Usar imagen base Java 17 Eclipse Temurin (más estable)
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Instalar Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copiar archivos Maven
COPY pom.xml .
COPY src ./src

# Compilar la aplicación
RUN mvn clean package -DskipTests

# Exponer puerto
EXPOSE 8083

# Ejecutar la aplicación
CMD ["java", "-jar", "target/external-service-0.0.1-SNAPSHOT.jar"]
