# Recommendation Microservice

## Overview

A Spring Boot 3.4.3 microservice for recommendations, built with Java 17, provides a robust, scalable solution for managing data in a distributed environment.
It utilizes RESTful APIs to enable communication between services, ensuring seamless user experience.
Leveraging Spring Boot's autoconfiguration, it simplifies deployment while offering flexibility through Spring Data for database interactions and Spring Security for authentication.

## Diagram
This is an overview of the project with a high-level diagram that explains the architecture and flow of the application and the microservice in question.

<img src="https://i.ibb.co/ynqCxv0f/Final-Class-Diagram.png" alt="Project Diagram" width="800"/>

## How to Run Locally

1. Clone the repository:
    ```bash
    git clone https://github.com/Grid-Java-Internship/recommendation-service
    cd recommendation-service
    ```

2. Install dependencies and build the application:
    ```bash
    ./gradlew build
    ```

3. Configure the environment:
    - Set any required environment variables (e.g., database URL, API keys).
    - You may want to configure `application.properties` for local development.

4. Start the application:
    ```bash
    ./gradlew bootRun
    ```
   Alternatively, you can build the JAR file and run it directly:
    ```bash
    ./gradlew build
    java -jar build/libs/<your-jar-name>.jar
    ```

## How to Run Tests

1. Run unit tests:
    ```bash
    ./gradlew test
    ```

2. Run integration tests (if applicable):
    ```bash
    ./gradlew verify
    ```

3. Run all tests (unit + integration):
    ```bash
    ./gradlew clean build
    ```

## Swagger Link

- You can access the Swagger documentation for the API at the following URL:
  [Swagger Documentation](http://localhost:8080/swagger-ui.html)

## Actuator / Grafana Links (for Monitoring)

- You can monitor application health, metrics, and other information via the following links:
    - [Actuator Health Check](http://localhost:8080/actuator/health)
    - [Actuator Metrics](http://localhost:8080/actuator/metrics)
    - Grafana Dashboard (TBD)

## Code Style

Please make sure to adhere to the following coding guidelines for consistent and readable code:

- **Indentation**: tab.
- **Naming Conventions**: Use camelCase for methods and variables, PascalCase for classes and interfaces, and UPPER_SNAKE_CASE for constants.
- **JavaDocs**: All public methods and classes should have appropriate JavaDocs.

## CI/CD

TBD

## Branching Strategy

We follow the [Git Flow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow) branching strategy, with the following branch types:

- **main**: The main production-ready branch.
- **{feature-name}**: Branches created from `main` for new features.
- **{bug-name}**: Branches created from `main` for bug fixes.

Before merging into `main`, make sure your branch passes all tests, has been reviewed, and follows the coding standards.