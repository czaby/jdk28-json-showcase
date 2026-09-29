#!/usr/bin/env bash
# Build the JDK 28 image and run the JUnit suite inside Docker.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

IMAGE="${IMAGE:-jdk28-json-showcase}"

if ! command -v docker >/dev/null 2>&1; then
  echo "error: docker is required" >&2
  exit 1
fi

echo "==> docker build ${IMAGE}"
docker build -t "$IMAGE" .

echo "==> docker run mvn test"
docker run --rm "$IMAGE" mvn -q test
