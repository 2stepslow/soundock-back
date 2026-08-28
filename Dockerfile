# 1단계: 빌드 전용 (JDK와 Gradle이 있는 환경)
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew clean bootJar --no-daemon -x test

# 2단계: 실행 전용 (JRE만 있는 가벼운 환경)
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", \
  "-Xmx160m", \
  "-XX:MaxMetaspaceSize=160m", \
  "-XX:ReservedCodeCacheSize=64m", \
  "-XX:+UseSerialGC", \
  "-Xss512k", \
  "-Duser.timezone=Asia/Seoul", \
  "-jar", "app.jar"]
