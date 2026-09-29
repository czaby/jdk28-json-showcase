#!/usr/bin/env bash
# example.IamPolicy — expand IAM policy Action/Resource string-or-array
# Argument: path to a policy JSON file (default: classpath /iam-policy.json)
set -euo pipefail
POLICY_JSON="${1:-}"
exec "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/run_in_docker.sh" example.IamPolicy ${POLICY_JSON:+"$POLICY_JSON"}
