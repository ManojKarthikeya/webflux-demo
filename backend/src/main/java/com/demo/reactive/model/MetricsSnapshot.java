package com.demo.reactive.model;

public record MetricsSnapshot(
    long timestamp,
    JvmMetrics jvm,
    HttpMetrics http,
    DbMetrics db
) {
    public record JvmMetrics(
        long heapUsed,
        long heapCommitted,
        long heapMax,
        long nonHeapUsed,
        int activeThreads,
        int peakThreads,
        int daemonThreads
    ) {}

    public record HttpMetrics(
        long totalRequests,
        double requestsPerSecond,
        double averageResponseTime,
        long activeConnections
    ) {}

    public record DbMetrics(
        double averageQueryTime,
        long activeConnections,
        long totalQueries
    ) {}
}
