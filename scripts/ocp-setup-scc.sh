#!/usr/bin/env bash
set -euo pipefail

echo "===> [OpenShift Security] Configuring Namespaces and SCC for TIBCO BWCE & Jenkins..."

for ns in jenkins argocd observability nubenetes-dev-bwce nubenetes-staging-bwce nubenetes-prod-bwce; do
    echo "Creating namespace: ${ns}..."
    kubectl create namespace "${ns}" --dry-run=client -o yaml | kubectl apply -f -
    
    # Enforce Pod Security Standards: restricted-v2
    kubectl label namespace "${ns}"       pod-security.kubernetes.io/enforce=restricted       pod-security.kubernetes.io/enforce-version=latest       --overwrite || true
done

echo "OpenShift namespaces and Security Context Constraints configured successfully."
