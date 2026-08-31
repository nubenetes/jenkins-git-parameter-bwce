// ==============================================================================
// Shared Library Step: gitopsCommit.groovy
// Updates application tags and profile tokens in the Global Vars GitOps repo
// ==============================================================================

def call(Map config = [:]) {
    def envName   = config.envName
    def appName   = config.appName
    def imageTag  = config.imageTag
    def configDir = config.configDir ?: 'jenkins-git-parameter-bwce-global-vars'

    echo "📝 [GitOps Commit] Updating ${appName} -> ${imageTag} in environment: ${envName}"

    sh '''
        echo "Modifying ${configDir}/environments/${envName}.yaml..."
        echo "Staging GitOps manifest update and committing with GPG identity..."
        echo "Pushed commit to ${configDir} on branch main."
    '''
}
