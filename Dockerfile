FROM flink:1.14.5-scala_2.11-java8
ENV FLINK_HOME=/opt/flink
WORKDIR $FLINK_HOME
COPY flink-dist/target/flink-1.14.5-bin/flink-1.14.5/lib/flink-dist_2.11-1.14.5.jar $FLINK_HOME/lib/flink-dist_2.11-1.14.5.jar
COPY flink-dist/target/flink-1.14.5-bin/flink-1.14.5/opt/flink-oss-fs-hadoop-1.14.5.jar $FLINK_HOME/plugins/oss-fs-hadoop/flink-oss-fs-hadoop-1.14.5.jar
RUN chown -R flink:flink .
