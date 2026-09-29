#!/usr/bin/env bash
# Build, test, and run the JDK 28 Simple JSON API showcase.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

die() { echo "error: $*" >&2; exit 1; }

need_cmd() { command -v "$1" >/dev/null 2>&1 || die "missing command: $1"; }

need_cmd java
need_cmd javac

JAVA_VER="$(java -XshowSettings:properties -version 2>&1 | awk -F= '/java.specification.version/ {gsub(/ /,"",$2); print $2}')"
case "$JAVA_VER" in
  28|29|30|31|32) ;;
  *)
    die "JDK 28+ is required (found java.specification.version=${JAVA_VER:-unknown}).
Download an early-access build from https://jdk.java.net/28/"
    ;;
esac

java --list-modules 2>/dev/null | grep -q '^jdk.incubator.json' \
  || die "this JDK has no jdk.incubator.json module"

if command -v mvn >/dev/null 2>&1; then
  echo "==> mvn test"
  mvn -q test
  echo "==> demo"
  java --add-modules jdk.incubator.json -cp target/classes dev.czaby.jdk28json.JsonShowcase
  exit 0
fi

echo "==> Maven not found; compiling with javac"
mkdir -p target/classes target/test-classes

javac --release 28 --add-modules jdk.incubator.json \
  -d target/classes \
  src/main/java/dev/czaby/jdk28json/*.java

echo "==> demo (tests skipped: install Maven to run JUnit)"
java --add-modules jdk.incubator.json -cp target/classes dev.czaby.jdk28json.JsonShowcase
