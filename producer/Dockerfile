FROM maven:3-openjdk-17-slim as build
WORKDIR /

RUN curl --location --output javaagent.jar 'https://dtdg.co/latest-java-tracer'

COPY pom.xml .
RUN mvn --batch-mode de.qaware.maven:go-offline-maven-plugin:resolve-dependencies

COPY ./src ./src
ENV MAVEN_OPTS='--add-opens java.base/java.lang=ALL-UNNAMED'
RUN mvn --batch-mode --offline package

FROM openjdk:17
COPY --from=build /javaagent.jar ./javaagent.jar
COPY --from=build /target/app.jar ./app.jar

ENTRYPOINT ["java", "-javaagent:./javaagent.jar", "-XX:FlightRecorderOptions=stackdepth=256", "-jar", "app.jar"]