#!/usr/bin/env bash
# example.JiraAdf — flatten ADF JSON.
# No args: bundled src/main/resources/adf-comment.json
# File path: parse that file
# Issue key: live GET /rest/api/3/issue/{key}?fields=comment (needs Jira env)
#
# Java reads:
#   JIRA_BASE_URL=https://your-site.atlassian.net
#   JIRA_EMAIL=you@example.com
#   JIRA_API_TOKEN=your-jira-api-token
set -euo pipefail

export JIRA_BASE_URL="${JIRA_BASE_URL:-https://your-site.atlassian.net}"
export JIRA_EMAIL="${JIRA_EMAIL:-you@example.com}"
export JIRA_API_TOKEN="${JIRA_API_TOKEN:-your-jira-api-token}"

exec "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/run_in_docker.sh" example.JiraAdf "$@"
