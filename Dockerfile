# Java 24 베이스 이미지 사용
FROM eclipse-temurin:24-jdk

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 복사
COPY build/libs/*.jar app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

