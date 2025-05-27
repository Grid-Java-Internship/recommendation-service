# multistage build
FROM gradle:8.12.1-jdk17 AS build

#GITHUB_CREDENTIALS
ARG GITHUB_USER_ARG
ARG GITHUB_TOKEN_ARG

# workdir of container
WORKDIR /app

# copy all project files to container
COPY . .

# run Gradle build (equivalent to mvn clean && mvn install -DskipTests)
RUN GITHUB_USER="$GITHUB_USER_ARG" GITHUB_TOKEN="$GITHUB_TOKEN_ARG" gradle clean build --no-daemon -x test

# lightweight image for runtime
FROM eclipse-temurin:17-jdk-alpine AS runtime

RUN addgroup --system --gid 1001 spring && \
    adduser --system --uid 1001 spring && \
    mkdir -p /home/spring/app

WORKDIR /home/spring/app

COPY --chown=spring:spring --from=build /app/build/libs/app.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]