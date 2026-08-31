# 🚀 Enterprise TIBCO BWCE Multi-Cluster GitOps Platform with Datadog Observability

[![TIBCO BWCE](https://img.shields.io/badge/TIBCO%20BWCE-2.9.2%20%2F%202.10.0-0080FF?logo=tibco)](https://docs.tibco.com/)
[![Datadog APM](https://img.shields.io/badge/Observability-Datadog%20APM%20%26%20CI%20Visibility-632CA6?logo=datadog)](https://www.datadoghq.com/)
[![Jenkins](https://img.shields.io/badge/Jenkins-2.492.2%20LTS-D24939?logo=jenkins)](https://www.jenkins.io/)
[![ArgoCD](https://img.shields.io/badge/GitOps-ArgoCD%203.5-EF7B42?logo=argo)](https://argo-cd.readthedocs.io/)
[![OpenShift](https://img.shields.io/badge/OpenShift-4.20%2B-EE0000?logo=redhatopenshift)](https://www.redhat.com/openshift)
[![SLSA Level 3](https://img.shields.io/badge/Supply%20Chain-SLSA%20Level%203-green?logo=linuxfoundation)](https://slsa.dev/)

An enterprise-grade, multi-cluster continuous integration and progressive delivery platform purpose-built for **TIBCO BusinessWorks™ Container Edition (BWCE 2.9.2 / 2.10.0)** microservices on **Red Hat OpenShift 4.20+**. It integrates **Jenkins Configuration as Code (JCasC)**, **Dynamic Git Parameter Plugin Dropdowns**, **Multi-Remote SCM**, **ArgoCD 3.5 Multi-Cluster GitOps**, and **Datadog Full-Stack Observability** (APM, DogStatsD, CI Visibility, Logs, and Argo Rollouts Metric Analysis).

---

## 📑 Table of Contents
- [Executive Summary & Architecture Overview](#executive-summary--architecture-overview)
- [TIBCO BWCE Cloud-Native Best Practices](#tibco-bwce-cloud-native-best-practices)
  - [1. 12-Factor Profile Externalization via `.substvar`](#1-12-factor-profile-externalization-via-substvar)
  - [2. Engine Sizing & Performance Tuning](#2-engine-sizing--performance-tuning)
  - [3. OpenShift Hardening & Health Probing](#3-openshift-hardening--health-probing)
- [Datadog Full-Stack Observability Architecture](#datadog-full-stack-observability-architecture)
  - [1. Jenkins CI/CD Visibility Plugin](#1-jenkins-cicd-visibility-plugin)
  - [2. TIBCO BWCE Java APM Tracer & DogStatsD](#2-tibco-bwce-java-apm-tracer--dogstatsd)
  - [3. Datadog Live Dashboards & Monitors](#3-datadog-live-dashboards--monitors)
  - [4. Progressive Delivery with Argo Rollouts & Datadog Metrics SLA](#4-progressive-delivery-with-argo-rollouts--datadog-metrics-sla)
- [Multi-Repository Git Parameter CI/CD Patterns](#multi-repository-git-parameter-cicd-patterns)
  - [Pattern 1: Dual Git Parameter Dropdowns (Multi-Remote SCM)](#pattern-1-dual-git-parameter-dropdowns-multi-remote-scm)
  - [Pattern 2: Decoupled CI Build & Multi-Cluster CD Orchestrator](#pattern-2-decoupled-ci-build--multi-cluster-cd-orchestrator)
- [Enterprise Security & Supply Chain Integrity](#enterprise-security--supply-chain-integrity)
  - [1. Cosign Image Signing & SLSA Level 3 Attestation](#1-cosign-image-signing--slsa-level-3-attestation)
  - [2. Zero-Trust Secrets with External Secrets Operator & Vault](#2-zero-trust-secrets-with-external-secrets-operator--vault)
  - [3. Ephemeral PR Preview Environments via ArgoCD ApplicationSets](#3-ephemeral-pr-preview-environments-via-argocd-applicationsets)
- [Repository Structure](#repository-structure)
- [Quick Start & Deployment](#quick-start--deployment)
- [Decommissioning & Reinstallation](#decommissioning--reinstallation)
- [References & Standards](#references--standards)

---

## Executive Summary & Architecture Overview

This platform delivers an automated, governed, multi-cluster CI/CD and GitOps delivery pipeline for enterprise TIBCO BWCE workloads across three OpenShift clusters (DEV, STAGING, PROD).

<details>
<summary>🗺️ <b>Click to expand: End-to-End Multi-Cluster Platform Topology Diagram</b></summary>
<br/>

```mermaid
flowchart TB
    subgraph DevSystems ["1. Developer and External Portals"]
        Dev["Developer / Ops"]
        Backstage["Backstage IDP<br/>Software Catalog"]
        ITSM["Jira Service Mgmt<br/>ServiceNow CMDB"]
    end

    subgraph OCP_DEV ["OpenShift Cluster 1: DEV (Control Plane and Workloads)"]
        direction TB
        subgraph JenkinsPlatform ["Jenkins Controller (JCasC and Job DSL)"]
            Master["Jenkins Controller<br/>2.492.2 LTS"]
            Seed["00-Seed-Job<br/>BWCE Orchestrator"]
            CIJob["01-CI-Build-Pipeline<br/>Git Param: BWCE App"]
            CDJob["02-CD-Release-Orchestrator<br/>Git Param: Global Vars"]
        end

        subgraph Agents ["Ephemeral Kubernetes Agent Pods"]
            BwceAgent["bwce-maven-builder Pod<br/>EAR Package and Test"]
            GitOpsAgent["argocd-gitops Pod<br/>Skopeo, Cosign, Argo CLI"]
        end

        subgraph OCPDevRegistry ["OCP DEV Internal Registry"]
            DevReg["image-registry:5000<br/>nubenetes-dev-bwce"]
        end

        subgraph ArgoCDMaster ["ArgoCD 3.5 Control Plane"]
            ArgoServer["ArgoCD Server and<br/>ApplicationSets"]
        end

        subgraph DatadogAgentStack ["Datadog Observability Stack"]
            DDAgent["Datadog Agent DaemonSet<br/>APM Port 8126 / DogStatsD 8125"]
            DDCluster["Datadog Cluster Agent<br/>Metrics Provider"]
        end

        DevApps["TIBCO BWCE DEV Workloads<br/>nubenetes-dev-bwce"]
    end

    subgraph OCP_STG ["OpenShift Cluster 2: STAGING (UAT)"]
        StgReg["OCP Staging Registry"]
        StgApps["TIBCO BWCE Staging Workloads<br/>nubenetes-staging-bwce"]
    end

    subgraph OCP_PRD ["OpenShift Cluster 3: PROD (High Availability)"]
        PrdReg["OCP Prod Registry"]
        PrdApps["TIBCO BWCE Production Workloads<br/>nubenetes-prod-bwce"]
        RolloutCtrl["Argo Rollouts Controller<br/>Canary Traffic Shifting"]
    end

    subgraph DatadogCloud ["Datadog Cloud Platform (datadoghq.eu)"]
        DDCIVis["CI/CD Visibility and Spans"]
        DDAPM["APM Tracing and Profiles"]
        DDDash["Dashboards and Monitors"]
    end

    Dev -->|Selects Branch or Tag| CIJob
    Dev -->|Selects Config Version| CDJob
    Backstage -->|REST API Trigger| CDJob
    ITSM -->|Webhook Dispatch| CDJob

    CIJob -->|Launches Pod| BwceAgent
    BwceAgent -->|Packages EAR and Builds Container| DevReg
    CIJob -->|Triggers Downstream| CDJob

    CDJob -->|Launches Pod| GitOpsAgent
    GitOpsAgent -->|Skopeo Copy| StgReg
    GitOpsAgent -->|Skopeo Copy| PrdReg
    GitOpsAgent -->|Sync and Health Check| ArgoServer

    ArgoServer -->|GitOps Deploy| DevApps
    ArgoServer -->|GitOps Deploy| StgApps
    ArgoServer -->|Sync Waves Deploy| PrdApps

    Master -.->|CI Spans and Logs| DDAgent
    DevApps -.->|APM Traces and Metrics| DDAgent
    StgApps -.->|APM Traces and Metrics| DDAgent
    PrdApps -.->|APM Traces and Metrics| DDAgent
    DDAgent -->|Forward Data| DatadogCloud
    RolloutCtrl -.->|Query p99 Latency and Error Rate| DatadogCloud
```

</details>

---

## TIBCO BWCE Cloud-Native Best Practices

### 1. 12-Factor Profile Externalization via `.substvar`
In enterprise TIBCO BWCE implementations, building environment-specific EAR files violates 12-Factor principles. Instead:
- **Build Once**: A single immutable `.ear` artifact (`tibco-bwce-order-service_2.1.0.ear`) is compiled during CI and packaged into the container image (`FROM tibco/bwce:2.9.2`).
- **Externalize Profiles**: Configuration tokens (`DEV.substvar`, `STAGING.substvar`, `PROD.substvar`) reside in the Single Source of Truth configuration repository: [`jenkins-git-parameter-bwce-global-vars`](https://github.com/nubenetes/jenkins-git-parameter-bwce-global-vars).
- **Runtime Injection**: The runtime profile is selected via `BW_PROFILE` environment variable or ConfigMap volume mount at container startup.

<details>
<summary>⚙️ <b>Click to expand: TIBCO BWCE Profile Externalization Flow Diagram</b></summary>
<br/>

```mermaid
flowchart LR
    subgraph BuildTime ["1. CI Packaging (Build Once)"]
        Source[".bwp Process Sources"] --> Maven["Maven BWCE Plugin"]
        Maven --> EAR["Single EAR Archive"]
        EAR --> BaseImage["FROM tibco/bwce:2.9.2"]
        BaseImage --> Image["Container Image<br/>Immutable Artifact"]
    end

    subgraph GitOpsSSOT ["2. Global Vars SSOT Repository"]
        DevVars["DEV.substvar<br/>dev.yaml"]
        StgVars["STAGING.substvar<br/>staging.yaml"]
        PrdVars["PROD.substvar<br/>prod.yaml"]
    end

    subgraph RuntimeDeploy ["3. OpenShift Runtime Deployment"]
        Image --> OCPDev["DEV Pod<br/>BW_PROFILE=DEV.substvar"]
        Image --> OCPStg["STAGING Pod<br/>BW_PROFILE=STAGING.substvar"]
        Image --> OCPPrd["PROD Pod<br/>BW_PROFILE=PROD.substvar"]

        DevVars -.->|Injected via K8s ConfigMap| OCPDev
        StgVars -.->|Injected via K8s ConfigMap| OCPStg
        PrdVars -.->|Injected via K8s ConfigMap| OCPPrd
    end
```

</details>

---

### 2. Engine Sizing & Performance Tuning
The BusinessWorks Container Edition runtime engine requires deliberate sizing parameters adapted to container cgroups:

| Environment | Replicas | CPU Req/Lim | Memory Req/Lim | `BW_ENGINE_THREADCOUNT` | `BW_STEP_FLOWLIMIT` | `BW_LOGLEVEL` |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **DEV** | 2 | 250m / 1000m | 512Mi / 1024Mi | `16` | `50` | `DEBUG` |
| **STAGING** | 3 | 500m / 1500m | 768Mi / 1536Mi | `32` | `100` | `INFO` |
| **PROD** | 6 | 1000m / 2000m | 1024Mi / 2048Mi | `64` | `250` | `WARN` |

- **`BW_ENGINE_THREADCOUNT`**: Specifies the number of engine worker threads executing process instances concurrently.
- **`BW_STEP_FLOWLIMIT`**: Prevents unbounded memory growth during traffic bursts by capping active in-memory process transitions.
- **`BW_CONTAINER_SHUTDOWN_TIMEOUT_SECONDS`**: Set to `30s` (DEV/STAGING) and `45s` (PROD) for clean process draining upon `SIGTERM`.
- **JVM Options**: `-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+ExplicitGCInvokesConcurrent` ensuring GC predictability inside Linux container cgroups.

---

### 3. OpenShift Hardening & Health Probing
- **Security Context Constraints**: Complies with OpenShift `restricted-v2` SCC (non-root `uid: 1001`, dropped capabilities `ALL`, read-only root filesystems).
- **Probes**:
  - **Liveness Probe**: `HTTP GET http://:8090/health` (Initial delay: `45s`, Period: `15s`, Timeout: `5s`).
  - **Readiness Probe**: `HTTP GET http://:8090/health` (Initial delay: `20s`, Period: `10s`, Timeout: `3s`).

---

## Datadog Full-Stack Observability Architecture

Replaces OpenTelemetry and Grafana with an enterprise **Datadog** observability stack.

<details>
<summary>📊 <b>Click to expand: Datadog Full-Stack Observability Architecture Diagram</b></summary>
<br/>

```mermaid
flowchart TD
    subgraph TelemetrySources ["1. OpenShift Workloads & Platform"]
        direction TB
        Jenkins["Jenkins Controller<br/>(Datadog Plugin 5.9.0)"]
        BWCEApp["TIBCO BWCE Microservice<br/>(Java Agent dd-java-agent.jar)"]
        RolloutCtrl["Argo Rollouts Controller<br/>(Progressive Canary)"]
    end

    subgraph DaemonSetLayer ["2. Node-Level Agent Layer"]
        direction TB
        DDAgent["Datadog Agent DaemonSet<br/>• APM Trace Port: 8126<br/>• DogStatsD Port: 8125<br/>• Container Log Collector<br/>• OpenMetrics Scraper (8090)"]
    end

    subgraph CloudPlatform ["3. Datadog Cloud Platform (datadoghq.eu)"]
        direction TB
        CIVisibility["CI/CD Visibility Engine<br/>Pipeline Spans & Build Analytics"]
        APMTrace["APM & Distributed Traces<br/>HTTP & JMS Transaction Flows"]
        OpenMetrics["Engine Metrics & Counters<br/>Threads, Active Jobs, JVM GC"]
        Dashboards["Live Dashboards<br/>• Jenkins CI Visibility<br/>• BWCE Engine Telemetry<br/>• ArgoCD GitOps Sync"]
        Monitors["Automated Alert Monitors<br/>• Error Rate > 1.0%<br/>• p99 Latency > 500ms<br/>• Build Queue Spikes"]
    end

    Jenkins -->|"CI Spans & Logs"| DDAgent
    BWCEApp -->|"APM Traces (Port 8126)"| DDAgent
    BWCEApp -->|"OpenMetrics (Port 8090)"| DDAgent
    BWCEApp -->|"Container Logs"| DDAgent

    DDAgent -->|"HTTPS Port 443"| CIVisibility
    DDAgent -->|"HTTPS Port 443"| APMTrace
    DDAgent -->|"HTTPS Port 443"| OpenMetrics

    CIVisibility --> Dashboards
    APMTrace --> Dashboards
    OpenMetrics --> Dashboards

    CIVisibility --> Monitors
    APMTrace --> Monitors
    OpenMetrics --> Monitors

    RolloutCtrl -.->|"Query p99 & 5xx SLA"| APMTrace
```

</details>

---

### 1. Jenkins CI/CD Visibility Plugin
The official Datadog Jenkins Plugin (`datadog:5.9.0`) is configured via JCasC (`jcasc/jenkins-jcasc.yaml`):
- Automatically correlates pipeline builds, stage execution times, agent queue wait times, and test results.
- Injects Datadog Trace IDs (`x-datadog-trace-id`) across pipeline steps.
- Global tags: `env:dev`, `team:integration-platform`, `tech:tibco-bwce`, `cluster:ocp-dev`.

### 2. TIBCO BWCE Java APM Tracer & DogStatsD
- **Java Tracer**: The Datadog Java APM agent (`dd-java-agent.jar`) is injected into the container image and attached to the BWCE engine via:
  ```bash
  BW_JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -javaagent:/opt/datadog/dd-java-agent.jar -Ddd.service=tibco-bwce-order-service -Ddd.logs.injection=true -Ddd.profiling.enabled=true"
  ```
- **OpenMetrics Autodiscovery**: Pod annotations configure the Datadog Agent to scrape BWCE management metrics from port `8090`:
  ```yaml
  annotations:
    ad.datadoghq.com/tibco-bwce-order-service.logs: '[{"source": "tibco-bwce", "service": "tibco-bwce-order-service"}]'
    ad.datadoghq.com/tibco-bwce-order-service.check_names: '["openmetrics"]'
    ad.datadoghq.com/tibco-bwce-order-service.init_configs: '[{}]'
    ad.datadoghq.com/tibco-bwce-order-service.instances: |
      [{"openmetrics_endpoint": "http://%%host%%:8090/metrics", "namespace": "tibco_bwce", "metrics": [".*"]}]
  ```

### 3. Datadog Live Dashboards & Monitors
Preconfigured Datadog JSON dashboards and alert monitors located in [`observability/`](observability/):
- **Jenkins CI Visibility**: [`observability/dashboards/datadog-jenkins-ci-visibility.json`](observability/dashboards/datadog-jenkins-ci-visibility.json)
- **BWCE Engine Performance**: [`observability/dashboards/datadog-bwce-runtime-performance.json`](observability/dashboards/datadog-bwce-runtime-performance.json)
- **ArgoCD GitOps Sync**: [`observability/dashboards/datadog-argocd-gitops-sync.json`](observability/dashboards/datadog-argocd-gitops-sync.json)
- **Automated Monitors**: [`observability/monitors/datadog-monitors-bwce.yaml`](observability/monitors/datadog-monitors-bwce.yaml) (Monitors HTTP 5xx error rate > 1%, p99 latency > 500ms, and build agent queue bottlenecks).

---

### 4. Progressive Delivery with Argo Rollouts & Datadog Metrics SLA
During canary deployments, Argo Rollouts queries Datadog metrics directly using [`sample-apps/tibco-bwce-order-service/rollout/analysis-template-datadog.yaml`](sample-apps/tibco-bwce-order-service/rollout/analysis-template-datadog.yaml):
- **Error Rate SLA**: Evaluates that 5xx errors $\le 0.1\%$ via `sum:trace.tibco_bwce_order_service.request.errors{env:prod}.as_count() / sum:trace.tibco_bwce_order_service.request.hits{env:prod}.as_count()`.
- **Latency SLA**: Evaluates that p99 latency $\le 250	ext{ms}$ via `p99:trace.tibco_bwce_order_service.request.duration{env:prod}` before traffic step promotion.

<details>
<summary>🐣 <b>Click to expand: Argo Rollouts Canary Traffic Splitting & Datadog Metric Analysis Diagram</b></summary>
<br/>

```mermaid
flowchart LR
    Ingress["OpenShift Route Traffic"]
    Canary["Canary Pods (10% -> 25% -> 50%)<br/>v2.1.0 (New BWCE EAR)"]
    Stable["Stable Pods (90% -> 75% -> 50%)<br/>v2.0.8 (Current BWCE EAR)"]
    DatadogMetrics["Datadog APM Metrics API<br/>5xx Rate <= 0.1% | p99 <= 250ms"]
    RolloutCtrl["Argo Rollouts Controller"]

    Ingress -->|Traffic Split| Canary
    Ingress -->|Traffic Split| Stable
    Canary -.->|Emits Traces and Metrics| DatadogMetrics
    DatadogMetrics -.->|Metric Evaluation| RolloutCtrl
    RolloutCtrl -->|Pass: Promote to 100%<br/>Fail: Automated Rollback| Ingress
```

</details>

---

## Multi-Repository Git Parameter CI/CD Patterns

### The Core Problem: Why Standard Pipelines Fail with Multiple Git Repositories
In cloud-native architectures, applications separate source code (`tibco-bwce-order-service`) from centralized configuration and Helm values (`jenkins-git-parameter-bwce-global-vars`).

When engineering teams attempt to introduce multiple `gitParameter` dropdowns into a single Jenkins Pipeline, they encounter a hard blocker where `GitSCM` appears to only support one repository, or secondary dropdowns fail.

<details>
<summary>⚠️ <b>Click to expand: Jenkins SCM Lifecycle & Pre-Execution Blindspot Diagram</b></summary>
<br/>

```mermaid
flowchart TB
    subgraph UI_Phase ["1. Pre-Execution Phase (Master)"]
        direction TB
        User["User opens Build UI Form"]
        Master["Master reads Job XML SCM"]
        GitParam["git-parameter queries remote refs"]
        Dropdown["Renders Dropdown for Primary Repo"]

        User --> Master --> GitParam --> Dropdown
    end

    subgraph Runtime_Phase ["2. Runtime Execution Phase (Agent)"]
        direction TB
        AllocAgent["Ephemeral Agent Pod Allocated"]
        RunStage["Pipeline Stage: checkout(repo-2)"]

        AllocAgent --> RunStage
    end

    Gap["SCM Blindspot:<br/>Dynamic stage checkouts run during<br/>build and are invisible at UI render"]

    Dropdown -.-> Gap
    Gap -.-> RunStage
```

</details>

---

### Pattern 1: Dual Git Parameter Dropdowns (Multi-Remote SCM)
*Target: Developer Sandboxes & Feature Preview Environments*
- **Dropdown 1**: Queries application repository (`APP_GIT_REVISION`).
- **Dropdown 2**: Queries global configuration repository (`GLOBAL_VARS_REVISION`).
- Multi-remote refspecs configured via Job DSL in [`jobdsl/pipelines-ci.groovy`](jobdsl/pipelines-ci.groovy).

### Pattern 2: Decoupled CI Build & Multi-Cluster CD Orchestrator
*Target: Enterprise Production Release Pipeline (Recommended)*

<details>
<summary>🧪 <b>Click to expand: Inner-Loop (Pattern 1) vs Outer-Loop (Pattern 2) Coexistence Diagram</b></summary>
<br/>

```mermaid
flowchart TD
    subgraph InnerLoop ["Inner-Loop (Pattern 1: Dual-Dropdown Pipeline)"]
        Dev1["Feature Developer"]
        DualJob["01-CI-*-ci-dual-dropdown<br/>Dropdown 1: App | Dropdown 2: Config"]
        DevSandbox["Ephemeral Dev Sandbox / PR Preview<br/>nubenetes-dev-bwce"]

        Dev1 -->|Interactive Trigger| DualJob
        DualJob -->|Direct Deploy| DevSandbox
    end

    subgraph OuterLoop ["Outer-Loop (Pattern 2: Decoupled CI + Multi-Cluster CD)"]
        Dev2["Team Lead / Release Manager"]
        CIJob["01-CI-*-ci-build<br/>Dropdown 1: App Repo Only"]
        Registry["OpenShift DEV Registry"]
        CDOrchestrator["02-CD-Release-Orchestrator<br/>Dropdown: Global Vars SSOT"]
        ArgoEngine["ArgoCD 3.5 Engine"]
        Clusters["Multi-Cluster Promotion Chain<br/>DEV -> STAGING -> PROD"]

        Dev2 -->|Trigger CI| CIJob
        CIJob -->|Push Image| Registry
        CIJob -->|Auto-Trigger with Parameters| CDOrchestrator
        CDOrchestrator -->|Sync Waves| ArgoEngine
        ArgoEngine -->|Promote and Verify| Clusters
    end
```

</details>

<details>
<summary>🔄 <b>Click to expand: End-to-End Hand-off Sequence Diagram</b></summary>
<br/>

```mermaid
sequenceDiagram
    autonumber
    actor Dev as Developer / Operator
    participant CI as 01-CI-Build-Pipeline
    participant DevReg as OCP DEV Registry
    participant CD as 02-CD-Release-Orchestrator
    participant StgReg as OCP Staging Registry
    participant PrdReg as OCP Prod Registry
    participant Argo as ArgoCD 3.5 Control Plane
    participant DDog as Datadog Cloud APM

    Dev->>CI: Trigger Build (Select App Branch)
    activate CI
    CI->>CI: Execute BWUnit Mock Tests
    CI->>CI: Package EAR and Build Container
    CI->>CI: Scan with Trivy and Sign with Cosign
    CI->>DevReg: Push image (e.g. 2.1.0-42)
    CI->>DDog: Emit CI Span (ci.bwce.pipeline_completed)
    CI->>CD: Dispatch downstream CD Orchestrator
    deactivate CI

    activate CD
    CD->>Argo: Sync DEV Application (Wave 0 -> Wave 1)
    Argo-->>CD: DEV Health Check Passed (HTTP 200)
    CD->>StgReg: Skopeo copy (DEV -> STAGING)
    CD->>Argo: Sync STAGING Application
    Argo-->>CD: STAGING Integration Suite Passed (100%)
    
    CD->>Dev: Production Approval Gate (Interactive Input)
    Dev-->>CD: Approved by Release Engineer

    CD->>PrdReg: Skopeo copy (STAGING -> PROD)
    CD->>Argo: Sync PROD Application (Argo Rollouts Canary)
    Argo->>DDog: Query Datadog Metric Analysis (p99 < 250ms, 5xx < 0.1%)
    DDog-->>Argo: Metrics Passed (100% Promoted)
    CD->>DDog: Emit Release Metric (cd.bwce.release_completed)
    deactivate CD
```

</details>

---

## Enterprise Security & Supply Chain Integrity

### 1. Cosign Image Signing & SLSA Level 3 Attestation
All container images layered on the BWCE base runtime are cryptographically signed using Sigstore Cosign during CI execution, guaranteeing supply chain integrity before promotion into production.

<details>
<summary>🔏 <b>Click to expand: Cosign Image Signing & SLSA Level 3 Attestation Flow Diagram</b></summary>
<br/>

```mermaid
flowchart LR
    CI["CI Build (Maven BWCE)"]
    Trivy["Trivy Scan and Syft SBOM"]
    Cosign["Cosign Signature and SLSA Attestation"]
    Registry["OCP DEV Registry"]
    Policy["OpenShift ImageSignaturePolicy<br/>Restricts unsigned images"]

    CI --> Trivy
    Trivy --> Cosign
    Cosign --> Registry
    Registry -.->|Enforced by| Policy
```

</details>

---

### 2. Zero-Trust Secrets with External Secrets Operator & Vault
Plaintext credentials (database passwords, JMS connection tokens, Datadog API keys) are never stored in Git. Instead, the **External Secrets Operator (ESO)** synchronizes encrypted secrets from HashiCorp Vault directly into OpenShift namespaces.

<details>
<summary>🔐 <b>Click to expand: External Secrets Operator & Vault Synchronization Flow Diagram</b></summary>
<br/>

```mermaid
flowchart LR
    Vault["HashiCorp Vault<br/>Raw Secrets and Keys"]
    GlobalVars["Global Vars Repo<br/>ExternalSecret Custom Resource"]
    ESO["External Secrets Operator<br/>OpenShift Controller"]
    K8sSecret["Native Kubernetes Secret<br/>Decrypted in Memory"]
    BWCEPod["TIBCO BWCE Pod<br/>Consumes Secret"]

    Vault -->|Fetched securely via ServiceAccount| ESO
    GlobalVars -->|Declares Secret Reference| ESO
    ESO -->|Generates / Rotates| K8sSecret
    K8sSecret -->|Mounted as Env / Volume| BWCEPod
```

</details>

---

### 3. Ephemeral PR Preview Environments via ArgoCD ApplicationSets
Pull Requests targeting application repositories automatically trigger the **ArgoCD PR Generator ApplicationSet**, creating ephemeral preview namespaces for end-to-end integration testing before merging.

<details>
<summary>⚡ <b>Click to expand: Ephemeral PR Preview Provisioning Flow Diagram</b></summary>
<br/>

```mermaid
flowchart LR
    Dev["Developer"]
    PR["GitHub Pull Request #42"]
    AppSet["ArgoCD ApplicationSet<br/>Pull Request Generator"]
    PreviewNS["Ephemeral Preview Namespace<br/>pr-42-preview"]
    Datadog["Datadog Preview Dashboard"]

    Dev -->|Opens| PR
    PR -->|Discovers PR| AppSet
    AppSet -->|Deploys BWCE App| PreviewNS
    PreviewNS -->|Injects DD_ENV=pr-42| Datadog
```

</details>

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
Set your Datadog credentials in `config/environments.env` or export environment variables:
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

## Decommissioning & Reinstallation

### Clean Decommission
```bash
./destroy.sh
# or
make destroy
```

### Full Reinstallation
```bash
./reinstall.sh
# or
make reinstall
```

---

## References & Standards
1. **TIBCO BWCE Documentation**: [TIBCO BusinessWorks™ Container Edition Official Docs](https://docs.tibco.com/)
2. **Datadog Jenkins Integration**: [Datadog CI Visibility & Jenkins Plugin](https://docs.datadoghq.com/continuous_integration/pipelines/jenkins/)
3. **Datadog Java Tracer APM**: [Datadog Java Agent Documentation](https://docs.datadoghq.com/tracing/trace_collection/dd_libraries/java/)
4. **ArgoCD 3.5 & ApplicationSets**: [ArgoCD Documentation](https://argo-cd.readthedocs.io/en/stable/)
5. **Sigstore Cosign**: [Cosign Container Signing Documentation](https://docs.sigstore.dev/cosign/overview/)
6. **External Secrets Operator**: [External Secrets Operator Docs](https://external-secrets.io/)
7. **Jenkins Job DSL & Git Parameter**: [Jenkins Job DSL API](https://jenkinsci.github.io/job-dsl-plugin/)

---

## 🔗 Related Repositories
- **Global Variables SSOT**: [jenkins-git-parameter-bwce-global-vars](https://github.com/nubenetes/jenkins-git-parameter-bwce-global-vars)
