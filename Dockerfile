FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -q dependency:go-offline
COPY src src
RUN ./mvnw -q package -DskipTests
RUN java -Djarmode=tools -jar target/*.jar extract --destination extracted

FROM eclipse-temurin:21-jre
ENV TZ=America/Sao_Paulo
ENV JAVA_TOOL_OPTIONS="-Xmx320m -Xss512k -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Duser.timezone=America/Sao_Paulo"
WORKDIR /app
COPY --from=build /app/extracted .
RUN java -XX:ArchiveClassesAtExit=app.jsa -Dspring.context.exit=onRefresh \
    -Dspring.flyway.enabled=false -Dspring.jpa.hibernate.ddl-auto=none \
    -Dspring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false \
    -Dspring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect \
    -jar Aure-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:SharedArchiveFile=app.jsa", "-jar", "Aure-0.0.1-SNAPSHOT.jar"]
