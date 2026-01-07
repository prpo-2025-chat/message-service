# =========================
# 1) Build stage
# =========================
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy only the Maven descriptor files first (better Docker layer caching)
COPY pom.xml /app/pom.xml
COPY api/pom.xml /app/api/pom.xml
COPY service/pom.xml /app/service/pom.xml
COPY entity/pom.xml /app/entity/pom.xml

# Download dependencies (cached unless pom files change)
RUN mvn -q -DskipTests dependency:go-offline \
  -Dmaven.wagon.http.retryHandler.count=5 \
  -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
  -Dmaven.wagon.http.timeout=60000 \
  -Dmaven.wagon.http.pool=false

# Now copy the actual source code
COPY . /app

# Build ONLY the api module (and whatever it depends on) and produce the Spring Boot fat jar
RUN mvn -q -DskipTests -pl api -am package

# =========================
# 2) Runtime stage
# =========================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/api/target/*.jar /app/app.jar

# Document the port (doesn't publish it; just informational)
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
