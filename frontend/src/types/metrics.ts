export interface JvmMetrics {
    heapUsed: number;
    heapCommitted: number;
    heapMax: number;
    nonHeapUsed: number;
    activeThreads: number;
    peakThreads: number;
    daemonThreads: number;
}

export interface HttpMetrics {
    totalRequests: number;
    requestsPerSecond: number;
    averageResponseTime: number;
    activeConnections: number;
}

export interface DbMetrics {
    averageQueryTime: number;
    activeConnections: number;
    totalQueries: number;
}

export interface MetricsSnapshot {
    timestamp: number;
    jvm: JvmMetrics;
    http: HttpMetrics;
    db: DbMetrics;
}
