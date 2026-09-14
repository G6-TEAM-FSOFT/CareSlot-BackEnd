# =======================================================
# Stage 1: Build JAR using Maven with Eclipse Temurin JDK 21
# =======================================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Package application (skipping tests for faster build)
RUN mvn clean package -DskipTests -B

# =======================================================
# Stage 2: Minimal Runtime Image using Eclipse Temurin JRE 21
# =======================================================
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Install tzdata and set timezone to Asia/Ho_Chi_Minh
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime && \
    echo "Asia/Ho_Chi_Minh" > /etc/timezone

ENV TZ="Asia/Ho_Chi_Minh"

# Create non-root user and group for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy jar from builder stage
COPY --from=builder /build/target/*.jar /app/app.jar

# Set file ownership
RUN chown -R spring:spring /app

USER spring:spring

EXPOSE 8080

# Environment options: allow overriding JVM options
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -Duser.timezone=Asia/Ho_Chi_Minh"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
