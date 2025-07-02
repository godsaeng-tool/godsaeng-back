# 멀티스테이지 빌드 - 안정적인 방법
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY src src
RUN gradle build --no-daemon -x test

FROM openjdk:17-jdk
WORKDIR /app

# JAR 파일 복사
COPY --from=build /app/build/libs/*.jar /app.jar

# 애플리케이션 실행
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"] 