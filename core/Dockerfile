FROM java:8u111-jre-alpine

# JRE fails to load fonts if there are no standard fonts in the image; DejaVu is a good choice,
# see https://github.com/docker-library/openjdk/issues/73#issuecomment-207816707
RUN apk add --update ttf-dejavu && rm -rf /var/cache/apk/*

ARG API_HTTP_PORT
ENV API_HTTP_PORT ${API_HTTP_PORT:-9091}

ARG JVM_MEM_MAX
ENV JVM_MEM_MAX ${JVM_MEM_MAX:-1348m}

RUN mkdir -p /etc/sangre/graphql-service \
 && mkdir -p /opt/sangre/graphql-service

COPY etc/* /etc/sangre/graphql-service/
COPY build/libs/graphql-service-*.jar /opt/sangre/graphql-service/graphql-service.jar

ENV RENESANS_ETC /etc/sangre/graphql-service
WORKDIR /opt/sangre/graphql-service

ENV JAVA_OPTS="-server -Xss512k -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+ParallelRefProcEnabled -Djava.net.preferIPv4Stack=true"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Xmx$JVM_MEM_MAX -Djava.security.egd=file:///dev/urandom -Dlogging.config=/etc/sangre/graphql-service/logback.xml -jar /opt/sangre/graphql-service/graphql-service.jar" ]

EXPOSE ${API_HTTP_PORT}
