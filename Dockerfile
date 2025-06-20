FROM eclipse-temurin:21-jdk as builder
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src ./src

RUN ./mvnw clean package -DskipTests


FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/target/music-*.jar app.jar

RUN mkdir -p /app/uploads/profile-images \
    && mkdir -p /app/uploads/event-images \
    && mkdir -p /app/logs/

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]