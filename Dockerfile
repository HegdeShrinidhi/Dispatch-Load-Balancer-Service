# Stage 1: Build the application using Maven wrapper
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy configuration files and source code
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src ./src

# Build the executable jar file safely skipping unit tests to maximize deployment speed
RUN ./mvnw clean package -DskipTests

# Stage 2: Create the final lightweight production image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Expose default port configuration parameters
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
