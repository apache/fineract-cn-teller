FROM openjdk:8-jdk-alpine

ARG teller_port=2028

ENV server.max-http-header-size=16384 \
    cassandra.clusterName="Test Cluster" \
    server.port=$teller_port

WORKDIR /tmp
COPY teller-service-boot-0.1.0-BUILD-SNAPSHOT.jar .

CMD ["java", "-jar", "teller-service-boot-0.1.0-BUILD-SNAPSHOT.jar"]
