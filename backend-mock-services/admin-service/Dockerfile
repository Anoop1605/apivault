FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/admin-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 9003
ENTRYPOINT ["java", "-jar", "app.jar"]
