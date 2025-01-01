# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-alpine

# Set environment variables
ENV PORT=8080

# Add a volume pointing to /tmp
VOLUME /tmp

# Set the working directory
WORKDIR /app

# Copy the application's JAR to the container
ARG JAR_FILE=build/libs/leviOMSOrder-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# Expose port
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java","-jar","app.jar"]
