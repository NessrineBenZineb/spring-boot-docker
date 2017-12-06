FROM openjdk:8-jdk-alpine
VOLUME /tmp
EXPOSE 8888
ADD /target/config-service-0.0.1-SNAPSHOT.jar //
CMD	java -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom -jar /config-service-0.0.1-SNAPSHOT.jar
