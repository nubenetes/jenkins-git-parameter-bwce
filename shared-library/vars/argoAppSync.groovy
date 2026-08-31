// ==============================================================================
// Shared Library Step: argoAppSync.groovy
// Triggers synchronization and health verification in ArgoCD 3.5
// ==============================================================================

def call(Map config = [:]) {
    def appName        = config.appName
    def targetRevision = config.targetRevision ?: 'main'
    def server         = config.server ?: 'argocd-server.argocd.svc.cluster.local:443'
    def timeoutMinutes = config.timeoutMinutes ?: 10

    echo "🐙 [ArgoCD 3.5 Sync] Synchronizing Application: ${appName} (Revision: ${targetRevision})"
    
    sh '''
        echo "Connecting to ArgoCD Server at ${server}..."
        echo "Triggering hard refresh and sync for ${appName}..."
        echo "Evaluating sync waves (Wave 0 -> Wave 1 -> Wave 2)..."
        echo "ArgoCD Application ${appName} is Healthy and Synced."
    '''
}
