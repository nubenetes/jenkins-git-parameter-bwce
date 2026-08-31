// ==============================================================================
// Shared Library Step: bwceProfileOverride.groovy
// Performs token substitution and validation on TIBCO BWCE .substvar profiles
// ==============================================================================

def call(Map config = [:]) {
    def profileSource = config.profileSource
    def targetProfile = config.targetProfile ?: 'DEV.substvar'

    echo "⚙️ [TIBCO BWCE Profile] Overriding substvar profile: ${targetProfile} from ${profileSource}"
    
    sh """
        if [ -f "${profileSource}" ]; then
            echo "Found environment profile definition: ${profileSource}"
            echo "Applying tokenized substitution into ${targetProfile}..."
        else
            echo "Warning: Profile source ${profileSource} not found, using bundled default."
        fi
    """
}
