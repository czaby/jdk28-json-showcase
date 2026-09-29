#!/usr/bin/env bash
# example.TerraformPlan — parse terraform show -json
# Argument: path to a plan JSON file (default: classpath /terraform-plan.json)
set -euo pipefail
PLAN_JSON="${1:-}"
exec "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/run_in_docker.sh" example.TerraformPlan ${PLAN_JSON:+"$PLAN_JSON"}
