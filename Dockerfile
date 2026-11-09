# Runtime image for the Spring Boot jar from the previous Gradle build.
# Build the jar first (`./gradlew build -x test`), then:
#   docker build -t kepegawaian .
FROM gcr.io/distroless/java25-debian13:nonroot

WORKDIR /app

# Only the executable Boot jar matches this pattern; `*-plain.jar` is excluded.
ARG JAR_FILE=build/libs/kepegawaian-*-SNAPSHOT.jar
COPY ${JAR_FILE} /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
