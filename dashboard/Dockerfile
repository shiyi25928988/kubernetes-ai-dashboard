FROM 10.113.16.240/maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy parent pom first
COPY pom.xml ./pom.xml

# Copy all module poms
COPY common/pom.xml ./common/pom.xml
COPY ai-base/pom.xml ./ai-base/pom.xml
COPY oss/pom.xml ./oss/pom.xml
COPY user/pom.xml ./user/pom.xml
COPY search/pom.xml ./search/pom.xml
COPY dashboard/pom.xml ./dashboard/pom.xml

# Download dependencies offline
RUN mvn dependency:go-offline -B

# Copy source code
COPY common/src ./common/src
COPY ai-base/src ./ai-base/src
COPY oss/src ./oss/src
COPY user/src ./user/src
COPY search/src ./search/src
COPY dashboard/src ./dashboard/src

# Build
RUN mvn clean package -DskipTests -B -pl dashboard -am

# List target directory to verify
RUN ls -la /app/dashboard/target/

# Runtime image
FROM 10.113.16.240/eclipse-temurin:21-jre-alpine

ENV TZ=Asia/Shanghai
WORKDIR /app

# Copy JAR from builder stage - find the jar file
COPY --from=builder /app/dashboard/target/*.jar /app/app.jar

# Verify the jar was copied
RUN ls -la /app/

# Expose port
EXPOSE 8080

# Startup command
CMD ["java", "-jar", "/app/app.jar"]
