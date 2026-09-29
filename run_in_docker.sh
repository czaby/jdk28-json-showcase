#!/usr/bin/env bash
# Build the JDK 28 image (unless SKIP_BUILD=1) and run a main class inside it.
# Usage: ./run_in_docker.sh example.ClassName [args...]
#
# Must use `java --add-modules`, not `mvn exec:java`. exec:java runs in Maven's
# JVM and does not see jdk.incubator.json (NoClassDefFoundError / module error).
#
# Java mains read these when present. Edit here or export before calling:
#   AWS_REGION=eu-central-1
#   AWS_ACCESS_KEY_ID=AKIA_YOUR_ACCESS_KEY
#   AWS_SECRET_ACCESS_KEY=your-secret-access-key
#   AWS_SESSION_TOKEN=           # optional, SSO/session
#   AWS_PROFILE=default          # optional; Docker still needs the key env vars
#   JIRA_BASE_URL=https://your-site.atlassian.net
#   JIRA_EMAIL=you@example.com
#   JIRA_API_TOKEN=your-jira-api-token
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

IMAGE="${IMAGE:-jdk28-json-showcase}"
CLASS="${1:-}"
if [[ -z "$CLASS" ]]; then
  echo "usage: $0 example.ClassName [args...]" >&2
  exit 2
fi
shift

if ! command -v docker >/dev/null 2>&1; then
  echo "error: docker is required" >&2
  exit 1
fi

if [[ "${SKIP_BUILD:-}" != 1 ]]; then
  echo "==> docker build ${IMAGE}"
  docker build -t "$IMAGE" .
fi

env_flags=()
for var in AWS_REGION AWS_ACCESS_KEY_ID AWS_SECRET_ACCESS_KEY AWS_SESSION_TOKEN AWS_PROFILE \
           JIRA_BASE_URL JIRA_EMAIL JIRA_API_TOKEN; do
  if [[ -n "${!var:-}" ]]; then
    env_flags+=(-e "$var")
  fi
done

echo "==> $CLASS${*:+ $*}"
docker run --rm "${env_flags[@]+"${env_flags[@]}"}" "$IMAGE" \
  java --add-modules jdk.incubator.json \
    -cp "target/classes:target/dependency/*" \
    "$CLASS" "$@"
