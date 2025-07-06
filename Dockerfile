FROM amazoncorretto:17 AS builder
WORKDIR /app

COPY gradle/ gradle/
COPY build.gradle settings.gradle gradlew ./
RUN ./gradlew dependencies --no-daemon

COPY src/ src/
RUN ./gradlew bootJar --no-daemon

FROM amazoncorretto:17
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
CMD [ "java", "-Dspring.profiles.active=prod", "-jar", "app.jar" ]