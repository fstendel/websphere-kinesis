FROM open-liberty:21.0.0.3-kernel-slim-java8-openj9

USER root
RUN apt-get update && \
    apt-get install wget && \
    mkdir jdbc && \
    wget -P jdbc https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.25/mysql-connector-java-8.0.25.jar && \
    chown -cR 1001 /jdbc && \
    apt-get -y purge wget
USER 1001


COPY --chown=1001:0 openliberty/server.xml /config/

RUN features.sh

COPY --chown=1001:0 websphere-kinesis-ear/target/websphere-kinesis-ear-1.0.0-SNAPSHOT.ear /config/dropins/

RUN configure.sh