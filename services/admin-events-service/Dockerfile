# Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# Run
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/admin-events-service-0.0.1-SNAPSHOT.jar app.jar
ENV JAVA_OPTS=""
EXPOSE 8086
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]