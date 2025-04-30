# -------- Stage 1: Build the application using Gradle --------
FROM gradle:8.12.1-jdk17 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy all project files to the container's working directory
COPY . .

# Build the application, skipping tests (similar to: mvn clean install -DskipTests)
RUN gradle clean build -x test

# -------- Stage 2: Create a lightweight runtime image --------
FROM eclipse-temurin:17-jdk-alpine AS runtime

# Create a non-root user and group for running the application
RUN addgroup --system --gid 1001 spring && \
    adduser --system --uid 1001 spring && \
    mkdir -p /home/spring/app

# Set the working directory for the runtime container
WORKDIR /home/spring/app

# Copy the built JAR from the build stage and set appropriate ownership
COPY --chown=spring:spring --from=build /app/build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8088

# Define the default command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
