FROM eclipse-temurin:11-jdk-alpine

WORKDIR app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

USER root

EXPOSE 8765/tcp

RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline

CMD ["./mvnw", "spring-boot:run"]