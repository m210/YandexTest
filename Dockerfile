FROM adoptopenjdk/openjdk11
EXPOSE 80
COPY target/YandexTest-0.0.1-SNAPSHOT.jar .
ENTRYPOINT ["java", "-jar", "YandexTest-0.0.1-SNAPSHOT.jar"]