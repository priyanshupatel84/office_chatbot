FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN chmod +x mvnw && ./mvnw -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/office_chatbot-0.0.1-SNAPSHOT.jar app.jar
COPY --from=build /workspace/knowledge /app/knowledge
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]