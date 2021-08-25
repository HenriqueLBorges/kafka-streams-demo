# Build stage
FROM maven:3.6.0-jdk-11-slim AS build
COPY pom.xml /app/
COPY src /app/src
RUN mvn -f /app/pom.xml clean package
RUN echo $(ls -1 /app/target/)

# Run stage
FROM openjdk:11-jre-slim
COPY --from=build /app/target/kafka-streams-app-1.0-SNAPSHOT.jar /app/app.jar
COPY --from=build /app/target/lib /app/lib
ENTRYPOINT ["java", "-jar", "/app/app.jar"]