FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/gateway-service-1.0.0-SNAPSHOT.jar app.jar
COPY src/main/resources/keys /app/src/main/resources/keys
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
