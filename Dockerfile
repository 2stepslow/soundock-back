#사용 이미지
FROM eclipse-temurin:21-jre-alpine

#jar 파일 컨테이너에 복사
COPY build/libs/*.jar app.jar

# 8080 포트 노출할거임
EXPOSE 8080

# 컨테이너 시작 시 실행될 명령어
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar", "--spring.profiles.active=aws"]