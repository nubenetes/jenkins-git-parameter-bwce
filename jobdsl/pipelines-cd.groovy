// ==============================================================================
// Job DSL: CD Release Orchestration & Multi-Cluster Promotion Pipelines (TIBCO BWCE)
// github.com/nubenetes/jenkins-git-parameter-bwce
// ==============================================================================

def globalVarsRepoUrl = 'https://github.com/nubenetes/jenkins-git-parameter-bwce-global-vars.git'

pipelineJob("02-CD-Release-Orchestrators/multi-cluster-release-orchestrator") {
    description('''
    🚀 <b>Enterprise Multi-Cluster TIBCO BWCE Release & Promotion Orchestrator</b><br/>
    Orchestrates promotion across 3 OpenShift clusters (DEV -> STAGING -> PROD) with ArgoCD 3.5 and Datadog.<br/>
    • Parameterized with <b>Git Parameter</b> on the BWCE Global Variables repository.<br/>
    • Can be triggered manually, by upstream CI build pipelines, or externally via REST API (Backstage IDP, ServiceNow ITSM, Jira).
    '''.stripIndent())

    logRotator {
        numToKeep(50)
        daysToKeep(60)
    }

    authenticationToken('RELEASE_DISPATCH_TOKEN_2026')

    parameters {
        // Git Parameter Plugin on BWCE Global Variables Repository
        gitParameterDefinition {
            name('GLOBAL_VARS_REVISION')
            type('PT_BRANCH_TAG')
            defaultValue('main')
            description("Select the Git Branch or Tag from the Global Configuration Repo (${globalVarsRepoUrl})")
            branch('')
            branchFilter('.*')
            tagFilter('*')
            sortMode('DESCENDING_SMART')
            selectedValue('DEFAULT')
            useRepository(globalVarsRepoUrl)
            quickFilterEnabled(true)
        }

        choiceParam('APP_NAME', ['tibco-bwce-order-service', 'tibco-bwce-customer-api', 'all-bwce-apps'], 'TIBCO BWCE Application to release and promote.')

        stringParam('IMAGE_TAG', 'latest', 'BWCE Container Image Tag to deploy and promote across OpenShift clusters.')

        choiceParam('TARGET_ENVIRONMENT', ['dev', 'staging', 'prod', 'full-promotion-chain'], 'Target environment or full promotional chain.')

        booleanParam('AUTO_PROMOTE_TO_STAGING', true, 'Automatically promote image and deploy to OCP STAGING cluster if DEV tests pass.')

        booleanParam('REQUIRE_PROD_APPROVAL', true, 'Require interactive human gate approval before deploying to OCP PROD cluster.')

        choiceParam('TRIGGERED_BY', ['MANUAL', 'CI_PIPELINE', 'BACKSTAGE_IDP', 'SERVICENOW_ITSM', 'JIRA_CMDB'], 'Caller identity for audit and trace metadata.')
        
        stringParam('CHANGE_REQUEST_ID', 'CHG-BWCE-0001', 'ITSM / Jira Change Request ID for production compliance.')
    }

    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('https://github.com/nubenetes/jenkins-git-parameter-bwce.git')
                    }
                    branch('*/main')
                }
            }
            scriptPath('jenkinsfiles/cd/Jenkinsfile.release-orchestrator')
            lightweight(true)
        }
    }
}

pipelineJob("02-CD-Release-Orchestrators/emergency-hotfix-deploy") {
    description('Emergency Hotfix deployment for TIBCO BWCE directly to target OpenShift cluster with fast-track ArgoCD sync.')
    
    parameters {
        gitParameterDefinition {
            name('HOTFIX_CONFIG_TAG')
            type('PT_TAG')
            defaultValue('')
            description('Select the verified Hotfix Tag from BWCE Global Vars')
            useRepository(globalVarsRepoUrl)
        }
        stringParam('APP_NAME', 'tibco-bwce-order-service', 'Target TIBCO BWCE application.')
        stringParam('HOTFIX_IMAGE_TAG', '', 'Hotfix container image tag.')
        choiceParam('TARGET_CLUSTER', ['ocp-prod', 'ocp-staging', 'ocp-dev'], 'Direct target cluster.')
        stringParam('EMERGENCY_INCIDENT_TICKET', 'INC-BWCE-911', 'Incident reference ticket.')
    }

    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('https://github.com/nubenetes/jenkins-git-parameter-bwce.git')
                    }
                    branch('*/main')
                }
            }
            scriptPath('jenkinsfiles/cd/Jenkinsfile.hotfix-deploy')
            lightweight(true)
        }
    }
}
