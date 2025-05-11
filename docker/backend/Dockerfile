# -------- Build Stage --------
FROM maven:3.9.9-amazoncorretto-21 AS build
WORKDIR /build

COPY ../../whatsappclone/pom.xml .
RUN mvn dependency:go-offline

COPY ../../whatsappclone/src ./src
RUN mvn clean package -DskipTests


# -------- Runtime Stage --------
FROM amazoncorretto:17
ARG PROFILE=dev
ARG APP_VERSION=1.0.0

WORKDIR /app

COPY --from=build /build/target/whatsappclone-*.jar /app/app.jar

EXPOSE 8080

# ✅ Variables d’environnement pour Spring Boot
ENV SPRING_PROFILES_ACTIVE=${PROFILE}
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/whatsapp_clone

CMD ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-Dspring.datasource.url=${SPRING_DATASOURCE_URL}", "app.jar"]

