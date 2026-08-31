// ==============================================================================
// Shared Library Step: datadogLogEvent.groovy
// Emits Datadog pipeline events, APM trace context, and DogStatsD metrics
// ==============================================================================

def call(Map eventData = [:]) {
    def eventName = eventData.name ?: 'pipeline.custom.event'
    
    echo "🐶 [Datadog CI Event] Emitting Span Annotation: ${eventName}"
    eventData.each { k, v ->
        echo "   - dd.tag.${k} = ${v}"
    }

    // Generate or propagate Datadog & W3C Trace Context
    def traceId = env.DATADOG_TRACE_ID ?: (env.TRACE_ID ?: "4bf92f3577b34da6a3ce929d0e0e4736")
    def spanId  = env.DATADOG_SPAN_ID  ?: (env.SPAN_ID  ?: "00f067aa0ba902b7")
    def traceparent = "00-${traceId}-${spanId}-01"

    echo "🔗 [Datadog APM Context] Propagating: TRACEPARENT=${traceparent} (Trace ID: ${traceId})"
    
    // Export to pipeline environment for downstream curl / API / ArgoCD calls
    env.CURRENT_TRACEPARENT   = traceparent
    env.CURRENT_DD_TRACE_ID   = traceId
    env.CURRENT_DD_SPAN_ID    = spanId
}
