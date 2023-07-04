FROM flink:1.17.1-scala_2.12-java8
ENV FLINK_HOME=/opt/flink
WORKDIR $FLINK_HOME
COPY flink-dist/target/flink-1.17.1-bin/flink-1.17.1/lib/flink-dist-1.17.1.jar $FLINK_HOME/lib/flink-dist-1.17.1.jar
COPY flink-dist/target/flink-1.17.1-bin/flink-1.17.1/lib/simpleclient-0.8.1.jar $FLINK_HOME/lib/simpleclient-0.8.1.jar
RUN chown -R flink:flink .

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
 && echo 'Asia/Shanghai' >/etc/timezone

RUN groupadd --gid=1002 dw \
 && useradd --uid=1002 --gid=dw dw \
 && mkdir -p /opt/hadoop/conf \
 && gosu flink mkdir -p /opt/flink/usrlib

# hadoop相关依赖，jenkins打包阶段从flume项目(/data1/dw/flume/plugins.d/hadoop)复制到(./hadoop/)
COPY ./hadoop/  /opt/hadoop/

ENV HADOOP_USER_NAME=dw
ENV HADOOP_CLASSPATH=/opt/hadoop/lib/*
