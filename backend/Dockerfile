#FROM openjdk:11-jdk
#VOLUME /tmp
#ARG JAR_FILE=build/libs/noning-0.0.1-SNAPSHOT.jar
#COPY ${JAR_FILE} app.jar
#EXPOSE 8080 # 이 컨테이너가 어떤 포트를 사용하는지
#ENTRYPOINT ["java","-jar","/app.jar"]
# open jdk java 17 버전의 환경을 구성
#FROM openjdk:17-jdk-oraclelinux7
#RUN yum install epel-release
#RUN yum localinstall --nogpgcheck https://download1.rpmfusion.org/free/el/rpmfusion-free-release-7.noarch.rpm \
#RUM yum install ffmpeg ffmpeg-devel
## build 시점에 활용되는 변수선언문 (target 폴더의 jar파일을 바라볼 것)
#ARG JAR_FILE=build/libs/backend-0.0.1-SNAPSHOT.jar
## 이를 카피하여 app.jar 로 복사
#COPY ${JAR_FILE} app.jar
#
## 이 컨테이너가 어떤 포트를 사용하는지
#EXPOSE 8999
## 기존 jar 파일을 단순 실행
#ENTRYPOINT ["java","-jar","/app.jar"]

FROM alpine:3.8 AS builder

WORKDIR /opt
ARG JDK_TAR=openjdk-17+28_linux-x64-musl_bin.tar.gz
ARG JDK_DOWNLOAD_PREFIX=https://download.java.net/java/early_access/alpine/28/binaries

RUN wget -q "$JDK_DOWNLOAD_PREFIX/$JDK_TAR" && \
    wget -q "$JDK_DOWNLOAD_PREFIX/$JDK_TAR.sha256"

RUN cat $JDK_TAR.sha256 | xargs -I{} echo "{}  $JDK_TAR" | sha256sum -c - && \
    tar zxf "$JDK_TAR" && \
    ln -s jdk-11 java && \
    rm -f "$JDK_TAR" "$JDK_TAR.sha256"

ENV JAVA_HOME=/opt/java
ENV PATH="$PATH:$JAVA_HOME/bin"

WORKDIR /root/dev
ENV GRADLE_USER_HOME=/root/dev/.cache
RUN mkdir -p /root/dev/.cache

COPY build.gradle gradlew gradlew.bat settings.gradle gradle.properties /root/dev/
COPY gradle /root/dev/gradle

RUN ./gradlew --no-daemon --refresh-dependencies --version

COPY src ./src
RUN ./gradlew --no-daemon build

# Make sure only 1 jar file is assembled
RUN test $(find ./build/libs -type f -name '*.jar' | wc -l) -eq 1

RUN jlink \
    --verbose \
    --module-path "$JAVA_HOME/jmods" \
    --add-modules java.base,java.logging,java.xml,jdk.unsupported,java.sql,java.naming,java.desktop,java.management,java.security.jgss,java.instrument,jdk.management \
    --compress 2 \
    --no-header-files \
    --no-man-pages \
    --strip-debug \
    --output /opt/jre

FROM alpine:3.8
WORKDIR /usr/src/app

ENV JAVA_HOME=/opt/jre
ENV LANG=C.UTF-8
ENV PATH="$PATH:$JAVA_HOME/bin"
ENV APP_TIMEZONE=UTC

COPY --from=builder /opt/jre /opt/jre
COPY --from=builder /root/dev/build/libs/*.jar dagger.jar

COPY newrelic/newrelic.jar newrelic/newrelic.yml ./

RUN apk add  --no-cache ffmpeg

EXPOSE 80

CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "dagger.jar"]