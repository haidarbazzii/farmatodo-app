# Usamos Eclipse Temurin (la distribución oficial y mantenida de OpenJDK)
FROM eclipse-temurin:25-jdk

# Directorio de trabajo
WORKDIR /app

# Copiamos el JAR (Asegúrate que el nombre coincida con el de la carpeta target)
COPY target/challenge-0.0.1-SNAPSHOT.jar app.jar

# Puerto
EXPOSE 8080

# Ejecutar
ENTRYPOINT ["java", "-jar", "app.jar"]