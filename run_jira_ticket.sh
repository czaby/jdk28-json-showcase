#!/usr/bin/env bash
# example.JiraTicket — GET /rest/api/2/issue/{key}?fields=labels,comment
#
# Java reads:
#   JIRA_BASE_URL     site origin, no trailing slash
#   JIRA_EMAIL        Atlassian account email
#   JIRA_API_TOKEN    https://id.atlassian.com/manage-profile/security/api-tokens
# First argument is the issue key (default ABC-123).
set -euo pipefail

export JIRA_BASE_URL="${JIRA_BASE_URL:-https://your-site.atlassian.net}"
export JIRA_EMAIL="${JIRA_EMAIL:-you@example.com}"
export JIRA_API_TOKEN="${JIRA_API_TOKEN:-your-jira-api-token}"
ISSUE="${1:-ABC-123}"

if [[ "$JIRA_BASE_URL" == "https://your-site.atlassian.net" || "$JIRA_API_TOKEN" == "your-jira-api-token" ]]; then
  echo "edit JIRA_BASE_URL / JIRA_EMAIL / JIRA_API_TOKEN in $0 (placeholders still set)" >&2
  exit 2
fi

exec "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/run_in_docker.sh" example.JiraTicket "$ISSUE"
