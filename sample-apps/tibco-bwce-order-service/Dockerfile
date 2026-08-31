# ==============================================================================
# TIBCO BusinessWorks Container Edition (BWCE) Enterprise Containerfile
# Base: tibco/bwce:2.9.2 | Datadog APM Java Agent & OpenShift Hardened
# ==============================================================================
FROM tibco/bwce:2.9.2

USER root

# Install Datadog Java APM Tracer
RUN mkdir -p /opt/datadog &&     curl -Lo /opt/datadog/dd-java-agent.jar https://dtdg.co/latest-java-tracer &&     chmod 644 /opt/datadog/dd-java-agent.jar

# Copy Compiled BWCE Application EAR
ADD target/tibco-bwce-order-service_*.ear /resources/addons/ea-app/

# Environment Variables & Default JVM Flags
ENV BW_LOGLEVEL=INFO \
    BW_ENGINE_THREADCOUNT=16 \
    BW_STEP_FLOWLIMIT=50 \
    BW_COMPONENT_HTTP_PORT=8080 \
    BW_APP_MONITORING_CONFIG=true \
    BW_CONTAINER_SHUTDOWN_TIMEOUT_SECONDS=30 \
    BW_JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -javaagent:/opt/datadog/dd-java-agent.jar -Ddd.logs.injection=true -Ddd.profiling.enabled=true"

# OpenShift non-root user compliance
USER 1001

EXPOSE 8080 8090
