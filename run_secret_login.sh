#!/usr/bin/env bash
# example.SecretLogin — Secrets Manager GetSecretValue, then Json.parse username/password.
#
# Java / AWS SDK default chain reads:
#   AWS_REGION=eu-central-1
#   AWS_ACCESS_KEY_ID=AKIA_YOUR_ACCESS_KEY
#   AWS_SECRET_ACCESS_KEY=your-secret-access-key
#   AWS_SESSION_TOKEN=                    # optional
# Argument: secret id (default app/demo/login). Secret JSON: {"username":"...","password":"..."}
set -euo pipefail

export AWS_REGION="${AWS_REGION:-eu-central-1}"
export AWS_ACCESS_KEY_ID="${AWS_ACCESS_KEY_ID:-AKIA_YOUR_ACCESS_KEY}"
export AWS_SECRET_ACCESS_KEY="${AWS_SECRET_ACCESS_KEY:-your-secret-access-key}"
export AWS_SESSION_TOKEN="${AWS_SESSION_TOKEN:-}"
SECRET_ID="${1:-app/demo/login}"

if [[ "$AWS_ACCESS_KEY_ID" == "AKIA_YOUR_ACCESS_KEY" || "$AWS_SECRET_ACCESS_KEY" == "your-secret-access-key" ]]; then
  echo "edit AWS_REGION / AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY in $0 (placeholders still set)" >&2
  echo "usage: $0 <secrets-manager-id>" >&2
  exit 2
fi

exec "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/run_in_docker.sh" example.SecretLogin "$SECRET_ID"
