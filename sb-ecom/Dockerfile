FROM eclipse-temurin:21-jdk AS build

# Cache-bust: 2026-08-06 force rebuild
LABEL build.timestamp="2026-08-06T07:10:00Z"

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src/ src/
RUN ./mvnw -B clean package -DskipTests

FROM eclipse-temurin:21-jdk

WORKDIR /app
RUN mkdir -p /app/images

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

# Keep PostgreSQL as the only supported JDBC driver even if a stale Render
# environment variable still sets SPRING_DATASOURCE_DRIVER_CLASS_NAME.
ENTRYPOINT ["java","-Dspring.datasource.driver-class-name=org.postgresql.Driver","-jar","app.jar"]
