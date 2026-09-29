# JDK 28 Simple JSON API showcase

Small, dependency-free demo of **JEP 540** (`jdk.incubator.json`) as it ships in JDK 28 early-access builds.

The API is an incubator. It can still change. It is not a Jackson / Gson replacement: no data binding, no streaming, no comments, no trailing commas.

Official references:

- https://openjdk.org/jeps/540
- https://download.java.net/java/early_access/jdk28/docs/api/jdk.incubator.json/module-summary.html

## Requirements

- JDK **28** or later (early-access is fine) with the incubating module `jdk.incubator.json`
- Maven 3.9+

Confirm the module is present:

```bash
java --list-modules | grep jdk.incubator.json
```

Download EA builds from https://jdk.java.net/28/

## Run

```bash
chmod +x run.sh
./run.sh
```

That script checks for JDK 28+, runs the JUnit suite when Maven is installed, then prints the demo.

Without Maven:

```bash
javac --release 28 --add-modules jdk.incubator.json \
  -d target/classes \
  src/main/java/dev/czaby/jdk28json/*.java

java --add-modules jdk.incubator.json -cp target/classes dev.czaby.jdk28json.JsonShowcase
```

With Maven:

```bash
mvn -q test
java --add-modules jdk.incubator.json -cp target/classes dev.czaby.jdk28json.JsonShowcase
```

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
