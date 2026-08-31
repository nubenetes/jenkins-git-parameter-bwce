# ==============================================================================
# Makefile: TIBCO BWCE Enterprise CI/CD & Multi-Cluster GitOps Platform
# ==============================================================================
.PHONY: help deploy destroy reinstall lint test

help:
	@echo "Available commands:"
	@echo "  make deploy     - Deploy Jenkins, Datadog Agent, ArgoCD, and BWCE apps"
	@echo "  make destroy    - Cleanly decommission all platform components"
	@echo "  make reinstall  - Full wipe and fresh redeployment"
	@echo "  make lint       - Validate YAML configurations"

deploy:
	./deploy.sh

destroy:
	./destroy.sh

reinstall:
	./reinstall.sh

lint:
	@echo "Validating configuration files..."
	@which yamllint >/dev/null 2>&1 && yamllint . || echo "yamllint not installed, skipped"
