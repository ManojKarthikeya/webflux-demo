package com.demo.reactive.service;

import com.demo.reactive.model.MetricsSnapshot;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.search.Search;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
public class MetricsService {

    private final MeterRegistry registry;

    public MetricsService(MeterRegistry registry) {
        this.registry = registry;
    }

    public Mono<MetricsSnapshot> getCurrentMetrics() {
        return Mono.fromSupplier(() -> {
            long timestamp = Instant.now().toEpochMilli();
            
            // JVM Metrics
            long heapUsed = (long) getGaugeValue("jvm.memory.used", "area", "heap");
            long heapCommitted = (long) getGaugeValue("jvm.memory.committed", "area", "heap");
            long heapMax = (long) getGaugeValue("jvm.memory.max", "area", "heap");
            long nonHeapUsed = (long) getGaugeValue("jvm.memory.used", "area", "nonheap");
            
            int activeThreads = (int) getGaugeValue("jvm.threads.live");
            int peakThreads = (int) getGaugeValue("jvm.threads.peak");
            int daemonThreads = (int) getGaugeValue("jvm.threads.daemon");

            MetricsSnapshot.JvmMetrics jvm = new MetricsSnapshot.JvmMetrics(
                heapUsed, heapCommitted, heapMax, nonHeapUsed,
                activeThreads, peakThreads, daemonThreads
            );

            // HTTP Metrics
            Timer httpTimer = registry.find("http.server.requests").timer();
            long totalRequests = httpTimer != null ? httpTimer.count() : 0;
            double avgResponseTime = httpTimer != null ? httpTimer.mean(TimeUnit.MILLISECONDS) : 0.0;
            // Simple approximation for RPS (this would ideally be a rate over time)
            double requestsPerSecond = httpTimer != null ? httpTimer.count() / Math.max(1, (System.currentTimeMillis() - 0) / 1000.0) : 0.0; 
            // Note: Real RPS calculation requires a sliding window or rate aggregation which Micrometer handles but exposing raw rate might need more logic.
            // For this demo, we might just use the count or a simple calculation if available.
            // Let's try to get a rate if possible, or just send the count and let frontend calculate rate.
            // Actually, let's just use the count for now and maybe 0 for RPS if not easily available without more complex setup.
            
            // Active connections might be available via other metrics depending on the server (Netty)
            // reactor.netty.http.server.connections.active
            long activeConnections = (long) getGaugeValue("reactor.netty.http.server.connections.active");

            MetricsSnapshot.HttpMetrics http = new MetricsSnapshot.HttpMetrics(
                totalRequests,
                0.0, // Placeholder for RPS
                avgResponseTime,
                activeConnections
            );

            // DB Metrics
            Timer dbTimer = registry.find("db.query").timer();
            double avgQueryTime = dbTimer != null ? dbTimer.mean(TimeUnit.MILLISECONDS) : 0.0;
            long totalQueries = dbTimer != null ? dbTimer.count() : 0;
            
            // Active connections - R2DBC pool metrics might be available if exposed
            // r2dbc.pool.acquired
            long dbActiveConnections = (long) getGaugeValue("r2dbc.pool.acquired");

            MetricsSnapshot.DbMetrics db = new MetricsSnapshot.DbMetrics(
                avgQueryTime, dbActiveConnections, totalQueries
            );

            return new MetricsSnapshot(timestamp, jvm, http, db);
        });
    }

    private double getGaugeValue(String name, String... tags) {
        Search search = registry.find(name);
        if (tags.length > 0) {
            search.tags(tags);
        }
        return search.gauge() != null ? search.gauge().value() : 0.0;
    }
}
