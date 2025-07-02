# 멀티스테이지 빌드로 이미지 크기 최적화
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY src src
RUN gradle build --no-daemon -x test

FROM openjdk:17-jre-slim
WORKDIR /app

# 필요한 패키지 설치
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 애플리케이션 사용자 생성
RUN groupadd -r appuser && useradd -r -g appuser appuser

# JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 사용자 권한 설정
RUN chown appuser:appuser app.jar
USER appuser

# 헬스체크 엔드포인트
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 애플리케이션 실행
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"] 