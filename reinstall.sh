#!/usr/bin/env bash
# ==============================================================================
# Full Wipe & Reinstallation Script
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "===> Triggering Platform Teardown..."
"${SCRIPT_DIR}/destroy.sh"

echo "===> Waiting 10s for namespace finalization..."
sleep 10

echo "===> Triggering Fresh Platform Deployment..."
"${SCRIPT_DIR}/deploy.sh"
