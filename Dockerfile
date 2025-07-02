FROM openjdk:17-jdk

# JAR 파일 복사
COPY build/libs/*SNAPSHOT.jar /app.jar

# 애플리케이션 실행
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"] 