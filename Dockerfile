FROM amazoncorretto:17-alpine AS builder
WORKDIR /app

COPY gradle/ gradle/
COPY build.gradle settings.gradle gradlew ./
COPY src/ src/

RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
EXPOSE 8080

COPY --from=builder /app/build/libs/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=""
ENTRYPOINT ["sh", "-c", "java -jar app.jar"]