#!/usr/bin/env bash
set -euo pipefail
if [[ "$#" -lt 1 ]]; then
  echo "usage: $0 <secrets-manager-id>" >&2
  exit 2
fi
exec "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/run_in_docker.sh" example.SecretLogin "$@"
