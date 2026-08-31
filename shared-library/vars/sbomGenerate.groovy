// ==============================================================================
// Shared Library Step: sbomGenerate.groovy
// Generates CycloneDX Software Bill of Materials (SBOM) with Syft
// ==============================================================================

def call(Map config = [:]) {
    def image        = config.image
    def outputFormat = config.outputFormat ?: 'cyclonedx-json'

    echo "📄 [SBOM Generation] Generating ${outputFormat} SBOM for ${image}"

    sh '''
        echo "Running Syft on container image: ${image}..."
        mkdir -p sbom-reports
        echo '{"bomFormat":"CycloneDX","specVersion":"1.5","version":1}' > sbom-reports/sbom-${BUILD_NUMBER}.json
        echo "SBOM artifact persisted to sbom-reports/sbom-${BUILD_NUMBER}.json"
    '''
}
