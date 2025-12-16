export interface MetricsSnapshot {
    memoryUsage: number;
    cpuUsage: number;
    threadCount: number;
    requestCount: number;
    timestamp: string;
}
