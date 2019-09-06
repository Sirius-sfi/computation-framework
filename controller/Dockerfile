FROM openjdk:8

ADD target/controller-0.1-SNAPSHOT.jar /usr/contoller/controller.jar

WORKDIR /usr/contoller

#ENTRYPOINT ["java", "-Ddomain=DEMO", "-jar", "controller.jar"]
ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-Ddomain=GEO_ASSISTANT", "-jar", "controller.jar"]