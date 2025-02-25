FROM openjdk:17-jdk

COPY target/TicketManagement-0.0.1-SNAPSHOT.jar .

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "TicketManagement-0.0.1-SNAPSHOT.jar"]
