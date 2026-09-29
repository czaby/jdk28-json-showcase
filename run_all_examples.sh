#!/usr/bin/env bash
# Build once, then start every example. Live AWS/Jira mains run only when env is set.
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
export IMAGE SKIP_BUILD=1

run() {
  echo
  echo "======== $*"
  "$ROOT/$1" "${@:2}"
}

run run_json_showcase.sh
run run_jira_adf.sh
run run_terraform_plan.sh
run run_iam_policy.sh

if [[ -n "${JIRA_BASE_URL:-}" && -n "${JIRA_EMAIL:-}" && -n "${JIRA_API_TOKEN:-}" ]]; then
  run run_jira_ticket.sh "${JIRA_ISSUE:-ABC-123}"
  run run_jira_adf.sh "${JIRA_ISSUE:-ABC-123}"
else
  echo
  echo "======== skip JiraTicket / live JiraAdf (set JIRA_BASE_URL, JIRA_EMAIL, JIRA_API_TOKEN)"
fi

if [[ -n "${SECRET_ID:-}" && -n "${AWS_REGION:-}${AWS_ACCESS_KEY_ID:-}${AWS_PROFILE:-}" ]]; then
  run run_secret_login.sh "$SECRET_ID"
else
  echo
  echo "======== skip SecretLogin (set SECRET_ID and AWS credentials)"
fi
