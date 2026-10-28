# AgriCoop Java Backend Dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY server-java/pom.xml ./server-java/pom.xml
COPY server-java/src ./server-java/src
COPY server-java/lib ./server-java/lib

RUN mvn -f server-java/pom.xml clean compile

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/server-java/target/classes ./server-java/target/classes
COPY --from=build /app/server-java/lib ./server-java/lib
COPY client ./client
COPY server-java/src/main/resources ./resources

EXPOSE 3000

ENV PORT=3000
ENV DB_URL=jdbc:mysql://mysql:3306/agricoop_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
ENV DB_USER=root
ENV DB_PASSWORD=root

ENTRYPOINT ["java", "-cp", "server-java/target/classes:server-java/lib/*:resources", "com.agricoop.AgriCoopServer"]
