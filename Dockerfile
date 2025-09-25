# ===== Stage 1: Build =====
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Copiamos wrapper/gradle config primero para cachear dependencias
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle gradle

# Copiamos el código fuente
COPY src src

# Damos permiso y compilamos el JAR
RUN chmod +x gradlew && ./gradlew --no-daemon clean bootJar

# ===== Stage 2: Runtime =====
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiamos el jar generado en el stage de build
COPY --from=build /workspace/build/libs/*.jar app.jar

# Render inyecta PORT; exponemos 8080 como default
EXPOSE 8080

# Perfil por defecto (Render puede overridear con env)
ENV SPRING_PROFILES_ACTIVE=prod

# Ejecutar la app usando PORT si está presente
ENTRYPOINT ["sh","-c","java -Dserver.port=${PORT:-8080} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -jar app.jar"]
