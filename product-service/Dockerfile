FROM openjdk:8-jdk-alpine
VOLUME /tmp
EXPOSE 8080
ADD /target/product-service-0.0.1-SNAPSHOT.jar //
CMD echo "The product will start after 100 ..." && \
    sleep 100  && \
	java -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom -jar /product-service-0.0.1-SNAPSHOT.jar