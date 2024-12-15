# Use a base image with Java 21
FROM maven:3.9.9-eclipse-temurin-21 AS builder

# Set the working directory in the container
WORKDIR /app

# Copy your pom.xml and src folder to the container
# Copy the Maven Wrapper and project files
COPY . .
# Build the Spring Boot project using Maven
RUN ./mvnw clean package -DskipTests

# Use a smaller image for the runtime environment
FROM amazoncorretto:21.0.4-alpine3.18

# Set the working directory for the runtime image
WORKDIR /app

RUN ls /app/crypto-finance-service/target/crypto-finance-service*.jar | head -n 1

# Copy the compiled jar file from the builder image to the runtime image
COPY --from=builder /app/crypto-finance-service/target/crypto-finance-service*.jar /app/app.jar

# Expose the port your Spring Boot app will run on
EXPOSE 8080

# Set the command to run your Spring Boot application
CMD ["java", "-jar", "/app/app.jar"]
