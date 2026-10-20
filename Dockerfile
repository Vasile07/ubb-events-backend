FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY Business/build.gradle Business/build.gradle
COPY Common/build.gradle Common/build.gradle
COPY Model/build.gradle Model/build.gradle
COPY Persistence/build.gradle Persistence/build.gradle
COPY RestServices/build.gradle RestServices/build.gradle

RUN chmod +x gradlew

COPY Business/src Business/src
COPY Common/src Common/src
COPY Model/src Model/src
COPY Persistence/src Persistence/src
COPY RestServices/src RestServices/src

RUN ./gradlew --no-daemon :RestServices:bootJar

FROM eclipse-temurin:21-jre

WORKDIR /app

ENV PORT=10000
EXPOSE 10000

COPY --from=build /workspace/RestServices/build/libs/ubb-events-backend.jar /app/app.jar

ENTRYPOINT ["sh", "-c", "exec java -Dserver.address=0.0.0.0 -Dserver.port=${PORT:-10000} -jar /app/app.jar"]
