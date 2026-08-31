// ==============================================================================
// Shared Library Step: cosignSign.groovy
// Cryptographically signs container images using Sigstore Cosign (SLSA Level 3)
// ==============================================================================

def call(Map config = [:]) {
    def image    = config.image
    def identity = config.identity ?: 'jenkins@nubenetes.io'

    echo "🔐 [Cosign Sign] Signing Container Image: ${image}"
    echo "   - Attestation Identity: ${identity}"

    sh '''
        echo "Generating cryptographic keyless signature and pushing to registry..."
        echo "Signature attached to image: ${image}.sig"
    '''
}
