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

```bash
chmod +x run.sh run_tests.sh run_in_docker.sh run_*.sh
```

All examples (bundled samples; live Jira/AWS only after you replace the placeholders in the scripts):

```bash
./run_all_examples.sh
```

Credentials live in the scripts as defaults you can edit. Shell exports win if already set.

```bash
# run_jira_ticket.sh / run_jira_adf.sh / run_all_examples.sh
JIRA_BASE_URL=https://your-site.atlassian.net
JIRA_EMAIL=you@example.com
JIRA_API_TOKEN=your-jira-api-token
JIRA_ISSUE=ABC-123

# run_secret_login.sh / run_all_examples.sh
AWS_REGION=eu-central-1
AWS_ACCESS_KEY_ID=AKIA_YOUR_ACCESS_KEY
AWS_SECRET_ACCESS_KEY=your-secret-access-key
AWS_SESSION_TOKEN=
SECRET_ID=app/demo/login
```

One class at a time:

```bash
./run.sh                      # example.JsonShowcase (same as ./run_json_showcase.sh)
./run_json_showcase.sh
./run_jira_adf.sh             # optional: ISSUE-KEY or a JSON file
./run_terraform_plan.sh       # optional: terraform-show.json
./run_iam_policy.sh           # optional: policy.json
./run_jira_ticket.sh          # default ABC-123; needs real JIRA_*
./run_secret_login.sh         # default app/demo/login; needs real AWS_*
./run_tests.sh
```

`run_in_docker.sh` is the shared runner. `SKIP_BUILD=1` reuses an already-built `IMAGE` (default `jdk28-json-showcase`).

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
