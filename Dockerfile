# Build stage
FROM eclipse-temurin:21-jdk as builder
WORKDIR /app

# Copy only the files needed for dependency resolution
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/music-*.jar app.jar

# Create directories for uploads and logs
RUN mkdir -p /app/uploads/profile-images \
    && mkdir -p /app/uploads/event-images \
    && touch /app/app.log

EXPOSE 8080 9090

ENTRYPOINT ["java", "-jar", "app.jar"]