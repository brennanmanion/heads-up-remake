FROM maven:3.8-openjdk-17-slim AS build

COPY pom.xml /app/
RUN mvn -f /app/pom.xml dependency:resolve

COPY src /app/src
RUN mvn -f /app/pom.xml verify

FROM openjdk:17-jdk-alpine

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT java -jar /app.jar