FROM docker.io/library/maven:3.9.16-eclipse-temurin-25-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

RUN ./mvnw dependency:go-offline

COPY src src

RUN ./mvnw clean package -DskipTests

FROM docker.io/library/eclipse-temurin:25-jre

WORKDIR /app

RUN useradd -ms /bin/bash spring

USER spring

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
