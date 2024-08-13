# Stage 1: Build the application using Gradle
FROM gradle:7.2.0-jdk11 as builder
WORKDIR /app
COPY . .
RUN gradle build -x test

# Stage 2: Create a lightweight container to run the application
FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=builder /app/build/libs/veterinaryapp-0.0.1-SNAPSHOT.jar veterinaryapp-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "/app/veterinaryapp-0.0.1-SNAPSHOT.jar"]