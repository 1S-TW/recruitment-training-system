# ===== Build stage =====
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN mvn -DskipTests package

# ===== Run stage =====
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Render sẽ cấp PORT (thường 10000). App phải đọc PORT này.
ENV PORT=8080
EXPOSE 8080

CMD ["sh","-c","java -jar app.jar --server.port=${PORT}"]
