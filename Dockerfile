# Imagen base JRE (Java 21; Spring Boot 3.5.x soporta 17+)
FROM eclipse-temurin:21-jre


# Directorio de trabajo
WORKDIR /app


# Copiamos el JAR generado por Gradle
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar


# Render inyecta PORT; exponemos 8080 como default
EXPOSE 8080


# Perfil activo por defecto (Render lo puede overridear con env)
ENV SPRING_PROFILES_ACTIVE=prod


# Ejecutar la app; usar PORT de Render si está presente
ENTRYPOINT ["sh","-c","java -Dserver.port=${PORT:-8080} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -jar app.jar"]