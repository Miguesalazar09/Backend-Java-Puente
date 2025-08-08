FROM openjdk:17-jdk-slim

WORKDIR /app

# Copiar archivos Maven
COPY pom.xml .
COPY src ./src

# Compilar la aplicación
RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests

# Exponer puerto
EXPOSE 8080

# Ejecutar la aplicación
CMD ["java", "-jar", "target/api-gateway-0.0.1-SNAPSHOT.jar"]
