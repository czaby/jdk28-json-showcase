# JDK 28 early-access + Maven, for JEP 540 (jdk.incubator.json).
# Official openjdk tags after 22 are EA-only; Temurin does not publish 28 yet.
# See https://hub.docker.com/_/openjdk and https://jdk.java.net/28/
FROM openjdk:28-ea-jdk-slim

RUN apt-get update \
    && apt-get install -y --no-install-recommends maven \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY pom.xml .
COPY src ./src

# Compile the demo at image-build time. Tests stay for run_tests.sh.
RUN mvn -q -DskipTests compile

CMD ["java", "--add-modules", "jdk.incubator.json", "-cp", "target/classes", "example.JsonShowcase"]
