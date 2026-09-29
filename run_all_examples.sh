#!/usr/bin/env bash
# Build once, then start every example.
#
# Live Jira / AWS run only after you replace the placeholders below
# (or export the same names in your shell).
#
#   JIRA_BASE_URL=https://your-site.atlassian.net
#   JIRA_EMAIL=you@example.com
#   JIRA_API_TOKEN=your-jira-api-token
#   JIRA_ISSUE=ABC-123
#
#   AWS_REGION=eu-central-1
#   AWS_ACCESS_KEY_ID=AKIA_YOUR_ACCESS_KEY
#   AWS_SECRET_ACCESS_KEY=your-secret-access-key
#   AWS_SESSION_TOKEN=
#   SECRET_ID=app/demo/login
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

export JIRA_BASE_URL="${JIRA_BASE_URL:-https://your-site.atlassian.net}"
export JIRA_EMAIL="${JIRA_EMAIL:-you@example.com}"
export JIRA_API_TOKEN="${JIRA_API_TOKEN:-your-jira-api-token}"
export JIRA_ISSUE="${JIRA_ISSUE:-ABC-123}"

export AWS_REGION="${AWS_REGION:-eu-central-1}"
export AWS_ACCESS_KEY_ID="${AWS_ACCESS_KEY_ID:-AKIA_YOUR_ACCESS_KEY}"
export AWS_SECRET_ACCESS_KEY="${AWS_SECRET_ACCESS_KEY:-your-secret-access-key}"
export AWS_SESSION_TOKEN="${AWS_SESSION_TOKEN:-}"
export SECRET_ID="${SECRET_ID:-app/demo/login}"

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

jira_ready() {
  [[ "$JIRA_BASE_URL" != "https://your-site.atlassian.net" && "$JIRA_API_TOKEN" != "your-jira-api-token" ]]
}

aws_ready() {
  [[ "$AWS_ACCESS_KEY_ID" != "AKIA_YOUR_ACCESS_KEY" && "$AWS_SECRET_ACCESS_KEY" != "your-secret-access-key" ]]
}

run run_json_showcase.sh
run run_jira_adf.sh
run run_terraform_plan.sh
run run_iam_policy.sh

if jira_ready; then
  run run_jira_ticket.sh "$JIRA_ISSUE"
  run run_jira_adf.sh "$JIRA_ISSUE"
else
  echo
  echo "======== skip live Jira (edit JIRA_* placeholders in $0)"
fi

if aws_ready; then
  run run_secret_login.sh "$SECRET_ID"
else
  echo
  echo "======== skip SecretLogin (edit AWS_* and SECRET_ID placeholders in $0)"
fi
