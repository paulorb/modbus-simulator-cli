# Modbus Simulator Cli - Paulo Balbino - 2024
FROM eclipse-temurin:17 as jre-build
ADD . /work
WORKDIR /work

RUN ./gradlew shadowJar

# Define your base image
FROM eclipse-temurin:17

# Continue with your application deployment
RUN mkdir /opt/app
COPY --from=jre-build work/build/libs/*.jar /opt/app/
ENTRYPOINT ["java", "-jar", "/opt/app/modbus-simulator-cli-1.0-SNAPSHOT-all.jar"]