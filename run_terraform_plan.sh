#!/usr/bin/env bash
# example.TerraformPlan
# Argument: path to either
#   terraform show -json plan.bin > plan.json     (one document)
#   terraform plan -json > plan.jsonl             (JSON sequence / JSONL)
# Default: classpath /terraform-plan.json
set -euo pipefail
PLAN_JSON="${1:-}"
exec "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/run_in_docker.sh" example.TerraformPlan ${PLAN_JSON:+"$PLAN_JSON"}
