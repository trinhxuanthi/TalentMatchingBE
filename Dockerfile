# Bước 1: Build project bằng Maven
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app

# TỐI ƯU HÓA: Copy riêng file pom.xml vào trước và tải thư viện (Cache)
# Nhờ bước này, các lần build sau nếu bro không thêm thư viện mới thì nó sẽ không phải tải lại mớ rác trên mạng về nữa, build nhanh gấp 10 lần!
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Sau đó mới copy thư mục source code vào và đóng gói
COPY src ./src
RUN mvn clean package -DskipTests

# Bước 2: Chạy ứng dụng (Giữ nguyên của bro vì chuẩn rồi)
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]