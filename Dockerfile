# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:17-jdk AS build

# Set the working directory in the container
WORKDIR /app

# Copy the gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Give execution permission to gradlew
RUN chmod +x ./gradlew

# Build the application
COPY src src
RUN ./gradlew bootJar --no-daemon

# Second stage: run the application
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the jar from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port 80
EXPOSE 80

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
