FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S opspilot && adduser -S opspilot -G opspilot
COPY --from=build /workspace/target/opspilot-ai-0.0.1-SNAPSHOT.jar app.jar
USER opspilot
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
