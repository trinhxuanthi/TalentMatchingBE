# Bước 1: Build project bằng Maven
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests -Dmaven.resources.skip=true
# Bước 2: Chạy ứng dụng (ĐÃ SỬA DÒNG NÀY)
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]