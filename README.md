# 🚀 Enterprise TIBCO BWCE Multi-Cluster GitOps Platform with Datadog Observability

[![TIBCO BWCE](https://img.shields.io/badge/TIBCO%20BWCE-2.9.2%20%2F%202.10.0-0080FF?logo=tibco)](https://docs.tibco.com/)
[![Datadog APM](https://img.shields.io/badge/Observability-Datadog%20APM%20%26%20CI%20Visibility-632CA6?logo=datadog)](https://www.datadoghq.com/)
[![Jenkins](https://img.shields.io/badge/Jenkins-2.492.2%20LTS-D24939?logo=jenkins)](https://www.jenkins.io/)
[![ArgoCD](https://img.shields.io/badge/GitOps-ArgoCD%203.5-EF7B42?logo=argo)](https://argo-cd.readthedocs.io/)
[![OpenShift](https://img.shields.io/badge/OpenShift-4.20%2B-EE0000?logo=redhatopenshift)](https://www.redhat.com/openshift)
[![SLSA Level 3](https://img.shields.io/badge/Supply%20Chain-SLSA%20Level%203-green?logo=linuxfoundation)](https://slsa.dev/)

An enterprise-grade, multi-cluster continuous integration and progressive delivery platform purpose-built for **TIBCO BusinessWorks™ Container Edition (BWCE)** microservices. It integrates **Jenkins Configuration as Code (JCasC)**, **Dynamic Git Parameter Plugin Dropdowns**, **Multi-Remote SCM**, **ArgoCD 3.5 Multi-Cluster GitOps**, and **Datadog Full-Stack Observability** (APM, DogStatsD, CI Visibility, Logs, and Argo Rollouts Metric Analysis).

---

## 🏛️ End-to-End Platform Architecture

```mermaid
flowchart TD
    subgraph Developer & IDP Interaction
        Dev["👩‍💻 Developer / ITSM / Backstage"] -->|Selects Branch / Tag| JenkinsUI["🚀 Jenkins Controller (JCasC + Job DSL)"]
    end

    subgraph Git Repositories (GitHub / Local)
        AppRepo["📦 BWCE App Repo<br/>(tibco-bwce-order-service)"]
        GlobalVarsRepo["🌐 Global Vars Repo<br/>(jenkins-git-parameter-bwce-global-vars)"]
    end

    subgraph Jenkins CI/CD Engine
        JenkinsUI -->|Git Parameter Dropdown 1| AppRepo
        JenkinsUI -->|Git Parameter Dropdown 2| GlobalVarsRepo
        JenkinsUI -->|Executes Pipeline| Agent["☸️ Ephemeral Pod Agent<br/>(bwce-maven-builder / skopeo-trivy)"]
        Agent -->|Builds EAR & Container Image| Registry["🐳 OpenShift Image Registry"]
        Agent -->|Emits Traces & Metrics| DDAgent["🐶 Datadog Agent / DogStatsD"]
    end

    subgraph Multi-Cluster GitOps & Progressive Delivery
        Agent -->|Updates Image Tag & Profile| GlobalVarsRepo
        GlobalVarsRepo -->|Watches & Syncs| ArgoCD["🐙 ArgoCD 3.5 (Multi-Cluster)"]
        ArgoCD -->|Sync Waves| DevCluster["☸️ OCP DEV Cluster"]
        ArgoCD -->|Promotes| StagingCluster["☸️ OCP STAGING Cluster"]
        ArgoCD -->|Canary Rollout| ProdCluster["☸️ OCP PROD Cluster"]
    end

    subgraph Observability & Reliability Gate
        DDAgent --> DatadogCloud["🐶 Datadog APM, Logs & CI Visibility"]
        ProdCluster -->|Argo Rollouts Metric Analysis| DatadogCloud
    end
```

---

## 🎯 Key Architectural Pillars

### 1. TIBCO BWCE Cloud-Native Best Practices
- **12-Factor Profile Externalization**:
  The application archive (`.ear`) is built once and promoted through DEV, STAGING, and PROD. Environment-specific token substitutions are managed through `.substvar` files (`DEV.substvar`, `STAGING.substvar`, `PROD.substvar`) injected via `BW_PROFILE` and Kubernetes ConfigMaps.
- **Engine Performance Tuning**:
  - `BW_ENGINE_THREADCOUNT`: Tuned per environment sizing (16 in DEV, 32 in STAGING, 64 in PROD).
  - `BW_STEP_FLOWLIMIT`: Throttles execution memory during burst traffic to prevent Out-Of-Memory (OOM).
  - `BW_CONTAINER_SHUTDOWN_TIMEOUT_SECONDS`: Graceful in-flight process draining upon SIGTERM.
- **OpenShift Hardening**:
  Compliant with OpenShift `restricted-v2` SCC (non-root `uid: 1001`, dropped capabilities, read-only root filesystems).
- **Probes**:
  Liveness and readiness probes integrated on BWCE management port `8090` at `/health`.

---

### 2. Full-Stack Datadog Observability Stack
Replaces OpenTelemetry & Grafana with native Datadog integrations across all layers:

| Layer | Datadog Integration | Details |
| :--- | :--- | :--- |
| **Jenkins CI/CD** | Datadog Jenkins Plugin (`5.9.0`) | Pipeline execution traces, stage durations, queue sizes, test visibility |
| **BWCE Runtime** | Datadog Java APM Tracer (`dd-java-agent.jar`) | Distributed tracing across HTTP & JMS calls, auto-instrumentation |
| **Metrics** | Datadog DogStatsD & OpenMetrics | Engine active processes, thread pool utilization, JVM GC metrics |
| **Log Management** | Datadog Log Collector | Automated container log parsing with trace/span injection |
| **Argo Rollouts** | Datadog Metric AnalysisTemplate | Canary validation querying Datadog API for error rate $\le 0.1\%$ & p99 latency $\le 250	ext{ms}$ |
| **Dashboards & Alerts** | Datadog Dashboards & Monitors | Out-of-the-box dashboards for Jenkins, BWCE Engine, and ArgoCD |

---

### 3. Dynamic Git Parameter Dropdowns (2 Operational Patterns)

#### Pattern 1: Dual-Dropdown Multi-Remote SCM (Sandbox & Preview)
Allows developers to select an application branch (`APP_GIT_REVISION`) and a configuration branch (`GLOBAL_VARS_REVISION`) simultaneously from two dynamic dropdowns.

#### Pattern 2: Decoupled CI Build & Multi-Cluster CD Orchestration (Production Standard)
- **CI Build Pipeline**: Triggers on application commits or Git Parameter dropdown on the application repo, builds EAR, scans with Trivy, signs with Cosign, pushes to registry, and triggers the downstream CD orchestrator.
- **CD Release Orchestrator**: Uses Git Parameter dropdown on `jenkins-git-parameter-bwce-global-vars`, orchestrating progressive promotion across DEV, STAGING, and PROD with ArgoCD 3.5 sync waves and approval gates.

---

## 📂 Repository Structure

```
.
├── config/
│   ├── environments.env              # Pinned versions, domain names, Datadog credentials
│   └── clusters.yaml                 # ConfigMap defining OCP DEV, STAGING, PROD topologies
├── helm/
│   ├── jenkins/                      # Official Jenkins chart values with Datadog plugin
│   ├── observability/                # Datadog Agent Helm values (OpenShift hardened)
│   └── argocd/                       # ArgoCD 3.5 Helm values
├── jcasc/
│   ├── jenkins-jcasc.yaml            # JCasC security matrix, Datadog CI visibility, seed job
│   ├── pod-templates.yaml            # Ephemeral Kubernetes agent pod templates (bwce-builder)
│   └── github-app-credentials.yaml   # GitHub App credential rotation
├── jobdsl/
│   ├── seed-job.groovy               # Master Seed Job definition
│   ├── pipelines-ci.groovy           # Pattern 1 and Pattern 2 CI pipelines
│   └── pipelines-cd.groovy           # CD Release Orchestrator & Hotfix pipelines
├── jenkinsfiles/
│   ├── ci/
│   │   ├── Jenkinsfile.app-bwce
│   │   └── Jenkinsfile.app-bwce-dual-dropdown
│   └── cd/
│       ├── Jenkinsfile.release-orchestrator
│       └── Jenkinsfile.hotfix-deploy
├── shared-library/
│   ├── vars/
│   │   ├── datadogLogEvent.groovy    # Emits Datadog CI spans & DogStatsD metrics
│   │   ├── bwceEarBuild.groovy       # Compiles and packages BWCE EAR
│   │   ├── bwceProfileOverride.groovy# Injects .substvar profile tokens
│   │   ├── argoAppSync.groovy        # ArgoCD 3.5 synchronization
│   │   ├── skopeoPromote.groovy      # Cross-cluster image promotion
│   │   ├── cosignSign.groovy         # Cryptographic image signing (SLSA Level 3)
│   │   ├── sbomGenerate.groovy       # CycloneDX SBOM generation
│   │   └── gitopsCommit.groovy       # Updates Global Vars SSOT repository
│   └── src/com/nubenetes/gitops/
├── sample-apps/
│   ├── tibco-bwce-order-service/     # Complete TIBCO BWCE project + Dockerfile + K8s + Rollout
│   └── tibco-bwce-customer-api/
├── observability/
│   ├── dashboards/                   # Datadog dashboards (Jenkins, BWCE Engine, ArgoCD)
│   └── monitors/                     # Datadog automated alert monitors
├── argocd-apps/                      # ArgoCD Root App-of-Apps & ApplicationSets
├── security/                         # Image signature policy & External Secrets Operator
├── scripts/                          # Automation & setup scripts
├── deploy.sh                         # 1-Click Platform Deployment Script
├── destroy.sh                        # Clean Decommission Script
├── reinstall.sh                      # Full Wipe & Reinstall Script
└── Makefile
```

---

## 🚀 Quick Start & Deployment

### 1. Prerequisites
- Red Hat OpenShift 4.20+ cluster (or Kubernetes 1.31+)
- `kubectl`, `oc`, and `helm` v3 CLI tools installed
- Datadog Account & API/App Keys

### 2. Configure Credentials
Set your Datadog API key in `config/environments.env` or via environment variables:
```bash
export DATADOG_API_KEY="your-datadog-api-key"
export DATADOG_APP_KEY="your-datadog-app-key"
```

### 3. Deploy Platform
```bash
./deploy.sh
# or
make deploy
```

### 4. Access Platform Services
- **Jenkins Controller**: `https://jenkins-jenkins.apps.ocp-dev.nubenetes.internal` (Default login: `admin` / `admin123!`)
- **ArgoCD UI**: `https://argocd-server.apps.ocp-dev.nubenetes.internal`
- **Datadog Dashboard**: `https://app.datadoghq.eu`

---

## 🔗 Related Repositories
- **Global Variables SSOT**: [jenkins-git-parameter-bwce-global-vars](https://github.com/nubenetes/jenkins-git-parameter-bwce-global-vars)
