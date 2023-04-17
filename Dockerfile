FROM eclipse-temurin:11-jdk-alpine

WORKDIR app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

USER root

EXPOSE 8765/tcp
EXPOSE 8765/udp
EXPOSE 8080/tcp
EXPOSE 8080/udp
EXPOSE 443/tcp
EXPOSE 443/udp

RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline

CMD ["./mvnw", "spring-boot:run"]