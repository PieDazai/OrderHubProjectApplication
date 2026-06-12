FROM maven:3.9.6-eclipse-temurin-21 AS builder

ARG APP_VERSION
ARG BUILD_DATE

LABEL version=${APP_VERSION}
LABEL build-date=${BUILD_DATE}
LABEL maintainer="OrderHub Team"

WORKDIR /app

COPY pom.xml ./pom.xml
COPY order-service/pom.xml ./order-service/pom.xml

WORKDIR /app/order-service

RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B \
    -Dmaven.wagon.http.retryHandler.count=3

COPY order-service/src ./src

RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine

ARG APP_VERSION
ARG BUILD_DATE

LABEL version=${APP_VERSION}
LABEL build-date=${BUILD_DATE}
LABEL description="OrderHub Production Application"
LABEL org.opencontainers.image.source="https://github.com/Oleborn/OrderHub"

WORKDIR /app

RUN apk add --no-cache curl

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder /app/order-service/target/*.jar app.jar

EXPOSE 8080

USER spring:spring

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]