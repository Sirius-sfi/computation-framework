FROM openjdk:8

ADD target/implementation-demo-0.1-SNAPSHOT.jar /usr/demo/demo.jar

WORKDIR /usr/demo

ENTRYPOINT ["java", "-Ddocker=true", "-jar", "demo.jar"]