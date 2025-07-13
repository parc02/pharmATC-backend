# Java 24 베이스 이미지 사용 (eclipse-temurin:24-jdk)
FROM eclipse-temurin:24-jdk

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사 (build/libs 폴더에서 app.jar로 복사)
COPY build/libs/*.jar app.jar

# 애플리케이션 실행 (app.jar 실행)
ENTRYPOINT ["java", "-jar", "app.jar"]

# 8080 포트 노출 (필요한 경우)
EXPOSE 8080

