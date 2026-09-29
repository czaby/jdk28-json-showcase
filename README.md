# JDK 28 Simple JSON API showcase

Small, dependency-free demo of **JEP 540** (`jdk.incubator.json`) as it ships in JDK 28 early-access builds.

The API is an incubator. It can still change. It is not a Jackson / Gson replacement: no data binding, no streaming, no comments, no trailing commas.

Official references:

- https://openjdk.org/jeps/540
- https://download.java.net/java/early_access/jdk28/docs/api/jdk.incubator.json/module-summary.html

## Requirements

- Docker (the demo and tests run inside `openjdk:28-ea-jdk-slim`)

The image installs Maven and compiles the project at build time. You do not need a local JDK 28.

## Run

Demo:

```bash
chmod +x run.sh run_tests.sh
./run.sh
```

That builds `jdk28-json-showcase` and runs `JsonShowcase` in the container.

Tests:

```bash
./run_tests.sh
```

Override the image name with `IMAGE=my-tag ./run.sh`.

Manual Docker:

```bash
docker build -t jdk28-json-showcase .
docker run --rm jdk28-json-showcase
docker run --rm jdk28-json-showcase mvn -q test
```

## CI

GitHub Actions (`.github/workflows/ci.yml`) builds the same image on every push and pull request, then runs `mvn test` and the showcase.

## What the demo covers

| Feature | Where |
|---|---|
| `Json.parse(String)` / `Json.parse(char[])` | `JsonShowcase.parseSection()` |
| `get(String)` / `get(int)` chaining | navigate |
| `tryGet(String)` for missing members | optional members |
| `tryValue()` for JSON `null` | null handling |
| `asString` / `asInt` / `asLong` / `asDouble` / `asBoolean` / `asMap` / `asList` | conversions |
| Pattern-matching `switch` on sealed `JsonValue` | type variance |
| Factories `JsonString.of`, `JsonNumber.of`, `JsonBoolean.of`, `JsonNull.of`, `JsonObject.of`, `JsonArray.of` | construction |
| `JsonValue.toString()` vs `Json.toDisplayString` | generation |
| `JsonParseException` (syntax + duplicate names) | parse errors |
| `JsonValueException` (wrong type / missing member) | access errors |

## Maven notes

There is no Maven artifact for this API. It lives in the JDK.

The POM only adds JUnit 5 for tests. Compiler and Surefire both pass `--add-modules jdk.incubator.json`.
