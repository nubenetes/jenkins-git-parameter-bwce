#!/usr/bin/env bash
set -euo pipefail

echo "===> [Credentials] Generating secure platform tokens & secrets..."
kubectl create secret generic jenkins-api-tokens   --from-literal=release-dispatch-token="RELEASE_DISPATCH_TOKEN_2026"   --namespace=jenkins   --dry-run=client -o yaml | kubectl apply -f -
