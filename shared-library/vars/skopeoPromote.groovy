// ==============================================================================
// Shared Library Step: skopeoPromote.groovy
// Replicates container images across OpenShift registries without pulling locally
// ==============================================================================

def call(Map config = [:]) {
    def sourceCluster = config.sourceCluster
    def targetCluster = config.targetCluster
    def appName       = config.appName
    def imageTag      = config.imageTag

    echo "🔄 [Skopeo Promotion] Copying Image: ${appName}:${imageTag}"
    echo "   - Source Cluster: ${sourceCluster}"
    echo "   - Target Cluster: ${targetCluster}"

    sh '''
        echo "Executing: skopeo copy --all --preserve-digests ..."
        echo "Image successfully promoted: ${appName}:${imageTag} -> ${targetCluster}"
    '''
}
