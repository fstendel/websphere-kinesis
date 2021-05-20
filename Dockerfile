FROM open-liberty:21.0.0.3-kernel-slim-java8-openj9

COPY --chown=1001:0 openliberty/server.xml /config/

RUN features.sh

COPY --chown=1001:0 websphere-kinesis-ear/target/websphere-kinesis-ear-1.0.0-SNAPSHOT.ear /config/dropins/

RUN configure.sh