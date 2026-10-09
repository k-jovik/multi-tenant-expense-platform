# Stage 1: build the JAR
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the pom first so dependency resolution is cached across code-only changes
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN ./mvnw -B dependency:go-offline

# Now copy source and build
COPY src src
RUN ./mvnw -B clean package -DskipTests

# Stage 2: run the JAR
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]