FROM openjdk:17

ENV KAFKA_VERSION=3.4.0
ENV KAFKA_SCALA_VERSION=2.13
RUN \
  curl https://downloads.apache.org/kafka/${KAFKA_VERSION}/kafka_${KAFKA_SCALA_VERSION}-${KAFKA_VERSION}.tgz \
  --location \
  --output /kafka.tgz \
  && tar -xzf /kafka.tgz \
  && rm /kafka.tgz

WORKDIR /kafka_${KAFKA_SCALA_VERSION}-${KAFKA_VERSION}

COPY ./provision.sh .
RUN ./provision.sh

ENTRYPOINT ["./bin/kafka-server-start.sh"]
CMD ["./config/kraft/server.properties"]