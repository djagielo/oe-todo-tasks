FROM openjdk:17-jdk-slim

WORKDIR /app

COPY /build/libs/oe-todo-tasks*.jar /app/overengineered-todo-tasks.jar

EXPOSE 9991

ENTRYPOINT ["java", "-jar", "overengineered-todo-tasks.jar"]