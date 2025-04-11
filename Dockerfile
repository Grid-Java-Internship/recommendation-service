# Build Application & Extract Layers
FROM gradle:8.12.1-jdk17 AS build
WORKDIR /app

# Copy only necessary build files first
# This layer is cached if these files don't change
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Download dependencies based on the build files
# This layer is cached if build files haven't changed
RUN gradle dependencies --no-daemon

# Copy the application source code
# This layer is invalidated only when source code changes
COPY src ./src

# Build the application using Spring Boot's layered JAR feature
# This runs only if source code (or build files) changed
# Requires enabling layered jars in build.gradle
RUN gradle bootJar --no-daemon -x test

# Use Spring Boot's layertools to extract the JAR into cacheable layers
RUN mkdir -p build/extracted && \
    java -Djarmode=layertools -jar build/libs/*.jar extract --destination build/extracted

# Use a JRE which is smaller than a full JDK
FROM eclipse-temurin:17-jre-alpine AS runtime

# Create a dedicated group and user to run the application for better security
RUN addgroup --system --gid 1001 spring && \
    adduser --system --uid 1001 spring --ingroup spring --shell /bin/false --disabled-password --home /home/spring

# Define app directory location
ARG APP_DIR=/home/spring/app

# Create app directory and set permissions before switching user
RUN mkdir -p ${APP_DIR} && \
    chown -R spring:spring ${APP_DIR}

# Set working directory
WORKDIR ${APP_DIR}

# Switch to the non-root user
USER spring

# Copy the extracted layers from the build stage
# Order matters for caching; dependencies change least often, application changes most often
COPY --chown=spring:spring --from=build /app/build/extracted/dependencies/ ./
COPY --chown=spring:spring --from=build /app/build/extracted/spring-boot-loader/ ./
COPY --chown=spring:spring --from=build /app/build/extracted/snapshot-dependencies/ ./
COPY --chown=spring:spring --from=build /app/build/extracted/application/ ./

# Expose port
EXPOSE 8088

# Use the Spring Boot loader launcher to run the application from the extracted layers
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]