# FROM openjdk:17
# MAINTAINER mihai
# COPY poller-1.0.0-SNAPSHOT.jar poller-1.0.0-SNAPSHOT.jar
# ENTRYPOINT ["java", "-jar", "/poller-1.0.0-SNAPSHOT.jar"]

# FROM vertx/vertx4:4.2.5

# ENV VERTICLE_NAME com.kry.poller.MainVerticle
# ENV VERTICLE_FILE /poller-1.0.0-SNAPSHOT.jar

# Set the location of the verticles
# ENV VERTICLE_HOME /usr/verticles

# EXPOSE 8080

# Copy your verticle to the container
# COPY $VERTICLE_FILE $VERTICLE_HOME/

# Launch the verticle
# WORKDIR $VERTICLE_HOME
# ENTRYPOINT ["sh", "-c"]
# CMD ["exec vertx run $VERTICLE_NAME -cp $VERTICLE_HOME/*"]

# 1st Docker build stage: build the project with Gradle
FROM gradle:7.4-jdk11 as builder
WORKDIR /project
COPY . /project/
RUN gradle assemble --no-daemon

# 2nd Docker build stage: copy builder output and configure entry point
FROM adoptopenjdk:11-jre-hotspot
ENV APP_DIR /application
ENV APP_FILE poller-1.0.0-SNAPSHOT.jar

EXPOSE 8080

WORKDIR $APP_DIR
COPY --from=builder /project/build/libs/*-fat.jar $APP_DIR/$APP_FILE

ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar $APP_FILE"]