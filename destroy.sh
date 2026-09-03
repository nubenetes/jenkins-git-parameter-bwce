#!/usr/bin/env bash
# ==============================================================================
# Clean Decommissioning Script: TIBCO BWCE Platform
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "======================================================================"
echo "⚠️  INITIATING CLEAN DECOMMISSION: TIBCO BWCE PLATFORM"
echo "======================================================================"

kubectl delete -f "${SCRIPT_DIR}/argocd-apps/" --ignore-not-found=true 2>/dev/null || true

if command -v helm &>/dev/null; then
    helm uninstall jenkins -n jenkins 2>/dev/null || true
    helm uninstall argocd -n argocd 2>/dev/null || true
    helm uninstall datadog-agent -n observability 2>/dev/null || true
fi

for ns in jenkins argocd observability nubenetes-dev-bwce nubenetes-staging-bwce nubenetes-prod-bwce; do
    echo "Deleting namespace: ${ns}..."
    kubectl delete namespace "${ns}" --ignore-not-found=true 2>/dev/null || true
done

echo "Platform decommission completed."
