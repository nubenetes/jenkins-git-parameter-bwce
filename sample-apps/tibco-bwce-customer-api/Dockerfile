FROM tibco/bwce:2.9.2
USER root
RUN mkdir -p /opt/datadog &&     curl -Lo /opt/datadog/dd-java-agent.jar https://dtdg.co/latest-java-tracer &&     chmod 644 /opt/datadog/dd-java-agent.jar
ADD target/tibco-bwce-customer-api_*.ear /resources/addons/ea-app/
ENV BW_COMPONENT_HTTP_PORT=8080 \
    BW_JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -javaagent:/opt/datadog/dd-java-agent.jar -Ddd.logs.injection=true"
USER 1001
EXPOSE 8080 8090
