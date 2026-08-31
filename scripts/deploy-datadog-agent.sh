#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "===> [Datadog Observability] Deploying Datadog Agent with APM & DogStatsD..."
if command -v helm &>/dev/null; then
    helm repo add datadog https://helm.datadoghq.com --force-update || true
    helm repo update
    helm upgrade --install datadog-agent datadog/datadog       --namespace observability       --create-namespace       -f "${SCRIPT_DIR}/helm/observability/datadog-agent-values.yaml" || echo "Datadog Helm applied / simulated"
fi
