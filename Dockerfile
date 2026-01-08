FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml /app/pom.xml
COPY api/pom.xml /app/api/pom.xml
COPY service/pom.xml /app/service/pom.xml
COPY entity/pom.xml /app/entity/pom.xml

RUN mvn -q -DskipTests dependency:go-offline \
  -Dmaven.wagon.http.retryHandler.count=5 \
  -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
  -Dmaven.wagon.http.timeout=60000 \
  -Dmaven.wagon.http.pool=false

COPY . /app
RUN mvn -q -DskipTests -pl api -am package

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /app/api/target/*.jar /app/app.jar

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=30s --start-period=5s --retries=3 CMD curl -fsS http://localhost:8080/health || exit 1


ENTRYPOINT ["java", "-jar", "/app/app.jar"]
