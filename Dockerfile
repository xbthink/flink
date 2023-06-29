FROM flink:1.17.1-scala_2.12-java8
ENV FLINK_HOME=/opt/flink
WORKDIR $FLINK_HOME
COPY flink-dist/target/flink-1.17.1-bin/flink-1.17.1/lib/flink-dist_2.12-1.17.1.jar $FLINK_HOME/lib/flink-dist_2.12-1.17.1.jar
COPY flink-dist/target/flink-1.17.1-bin/flink-1.17.1/opt/flink-oss-fs-hadoop-1.17.1.jar $FLINK_HOME/plugins/oss-fs-hadoop/flink-oss-fs-hadoop-1.17.1.jar
RUN chown -R flink:flink .
