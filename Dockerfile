# Build stage
FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

# Install Maven
RUN apk add --no-cache maven

# Copy only the POM first to cache dependencies
COPY pom.xml .
COPY src src

# Build application
RUN mvn dependency:go-offline

# Executar testes e construir
RUN mvn test && \
    echo "=== TEST RESULTS ===" && \
    cat target/surefire-reports/*.txt && \
    mvn package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy jar from build stage
COPY --from=build /workspace/app/target/*.jar app.jar

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"] 