package com.nubenetes.gitops

class ClusterEnvironment implements Serializable {
    String name
    String environment
    String apiEndpoint
    String appsDomain
    String registryHost

    ClusterEnvironment(String name, String environment, String appsDomain) {
        this.name = name
        this.environment = environment
        this.appsDomain = appsDomain
        this.apiEndpoint = "https://api.${appsDomain}:6443"
        this.registryHost = "image-registry.openshift-image-registry.svc:5000"
    }

    String getRouteUrl(String appName) {
        return "https://${appName}.${this.appsDomain}"
    }
}
