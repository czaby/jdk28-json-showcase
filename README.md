# JDK 28 Simple JSON API showcase

Small demo of **JEP 540** (`jdk.incubator.json`) as it ships in JDK 28 early-access builds.

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

That builds `jdk28-json-showcase` and runs `example.JsonShowcase` in the container.

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

JSON-heavy examples (bundled sample documents; pass a file to override):

```bash
docker run --rm jdk28-json-showcase java --add-modules jdk.incubator.json -cp target/classes example.JiraAdf
docker run --rm jdk28-json-showcase java --add-modules jdk.incubator.json -cp target/classes example.TerraformPlan
docker run --rm jdk28-json-showcase java --add-modules jdk.incubator.json -cp target/classes example.IamPolicy
```

`JiraAdf` can also flatten the last comment of a live issue (API v3 ADF body):

```bash
docker run --rm \
  -e JIRA_BASE_URL -e JIRA_EMAIL -e JIRA_API_TOKEN \
  jdk28-json-showcase \
  java --add-modules jdk.incubator.json -cp target/classes example.JiraAdf ABC-123
```

Secrets Manager example (`example.SecretLogin`) uses the default AWS credential chain. CI does not invoke it:

```bash
docker run --rm \
  -e AWS_REGION -e AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY -e AWS_SESSION_TOKEN \
  jdk28-json-showcase \
  mvn -q exec:java -Dexec.mainClass=example.SecretLogin -Dexec.args='my/secret/id'
```

Jira labels example (`example.JiraTicket`) calls REST v2 for `ABC-123` unless you pass another key. Needs `JIRA_BASE_URL`, `JIRA_EMAIL`, and `JIRA_API_TOKEN`. CI does not invoke it:

```bash
docker run --rm \
  -e JIRA_BASE_URL -e JIRA_EMAIL -e JIRA_API_TOKEN \
  jdk28-json-showcase \
  mvn -q exec:java -Dexec.mainClass=example.JiraTicket -Dexec.args='ABC-123'
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
| Secrets Manager `username` / `password` | `SecretLogin` |
| Jira labels and last comment | `JiraTicket` |
| Flatten Jira ADF `content` trees | `JiraAdf` |
| `terraform show -json` resource_changes | `TerraformPlan` |
| IAM `Action`/`Resource` string-or-array | `IamPolicy` |

## Maven notes

The JSON API lives in the JDK (`--add-modules jdk.incubator.json`). JUnit 5 is for tests. `software.amazon.awssdk:secretsmanager` is only for `SecretLogin`.
