# Capability: System Metrics Dashboard

Live system monitoring displaying JVM metrics, request statistics, and database performance using hybrid REST + SSE pattern.

## ADDED Requirements

### Requirement: Collect JVM metrics with Micrometer

The backend SHALL expose JVM and application metrics using Spring Boot Actuator and Micrometer.

**Priority:** High  
**Status:** Planned

#### Scenario: Expose JVM memory metrics

**Given** the application is running  
**When** metrics are collected  
**Then** JVM heap memory usage (used, committed, max) is available  
**And** non-heap memory metrics are available  
**And** metrics are accessible via Micrometer registry

#### Scenario: Expose thread pool metrics

**Given** the Netty event loop is processing requests  
**When** metrics are collected  
**Then** active thread count is recorded  
**And** peak thread count is recorded  
**And** thread state breakdown (runnable, waiting, blocked) is available

#### Scenario: Track HTTP request metrics

**Given** the application receives HTTP requests  
**When** requests are processed  
**Then** request count is incremented  
**And** request duration histogram is updated  
**And** status code distribution (2xx, 4xx, 5xx) is tracked

### Requirement: Provide initial metrics snapshot via REST

The frontend SHALL fetch current metrics state via REST endpoint for fast initial page load.

**Priority:** High  
**Status:** Planned

#### Scenario: Fetch current metrics snapshot

**Given** the metrics dashboard page loads  
**When** the frontend calls `GET /api/metrics`  
**Then** the backend returns current snapshot containing:
  - CPU usage percentage
  - Memory usage (heap and non-heap)
  - Active thread count
  - Request rate (requests per second)
  - Average response time
**And** the response completes within 100ms  
**And** TanStack Query caches the result

#### Scenario: Metrics snapshot uses reactive aggregation

**Given** metrics are stored in Micrometer registry  
**When** the snapshot is requested  
**Then** the service aggregates metrics using `Mono.zip()` for parallel collection  
**And** all metric queries execute concurrently (non-blocking)  
**And** the combined result is returned as `Mono<MetricsSnapshot>`

### Requirement: Stream live metric updates via SSE

The system SHALL push incremental metric updates to connected clients using Server-Sent Events.

**Priority:** High  
**Status:** Planned

#### Scenario: Stream metrics every 2 seconds

**Given** a client connects to `/api/metrics/stream`  
**When** the SSE stream is established  
**Then** the backend emits metric updates every 2 seconds  
**And** each event contains delta or current values for all metrics  
**And** the stream continues until client disconnects

#### Scenario: Metric updates reflect real-time changes

**Given** the application is processing varying load  
**When** request rate increases from 10 to 50 req/s  
**Then** the next SSE event shows updated request rate  
**And** the frontend chart reflects the change within 2 seconds  
**And** memory and thread metrics also update accordingly

#### Scenario: Multiple clients receive independent streams

**Given** 3 clients are connected to metrics stream  
**When** a metric event is emitted  
**Then** all 3 clients receive the same data  
**And** each client maintains independent SSE connection  
**And** disconnection of one client does not affect others

### Requirement: Visualize metrics with live charts

The frontend SHALL display metrics using interactive charts that update in real-time.

**Priority:** High  
**Status:** Planned

#### Scenario: Display memory usage chart

**Given** the metrics dashboard is loaded  
**When** viewing memory metrics  
**Then** a line chart shows heap memory over time (last 60 data points)  
**And** the chart has two lines: used memory and committed memory  
**And** the Y-axis scales dynamically to max memory value  
**And** new data points append smoothly with animation

#### Scenario: Display request throughput chart

**Given** requests are being processed  
**When** viewing throughput metrics  
**Then** a bar chart shows requests per second for last 30 intervals  
**And** bars update in real-time as new data arrives  
**And** color coding indicates load level (green < 20, yellow 20-50, red > 50 req/s)

#### Scenario: Display thread count gauge

**Given** thread metrics are available  
**When** viewing thread information  
**Then** a gauge chart displays current active thread count  
**And** the gauge maximum is set to available processor count × 10  
**And** the gauge needle animates smoothly on updates

### Requirement: Track database query performance

The system SHALL measure and report R2DBC query execution times.

**Priority:** Medium  
**Status:** Planned

#### Scenario: Record query execution time

**Given** a reactive database query is executed  
**When** the repository method completes  
**Then** query duration is recorded in Micrometer timer  
**And** timer includes percentiles (p50, p95, p99)  
**And** timer is tagged with operation type (SELECT, INSERT, UPDATE)

#### Scenario: Display database performance metrics

**Given** database queries are being executed  
**When** viewing the metrics dashboard  
**Then** average query time is displayed  
**And** p95 and p99 percentiles are shown  
**And** query count per operation type is displayed  
**And** metrics update via SSE stream

#### Scenario: Slow query detection

**Given** a query takes longer than 1 second  
**When** the query completes  
**Then** a slow query counter increments  
**And** the slow query count appears in metrics dashboard  
**And** the metric is highlighted in red if count > 0

### Requirement: Display real-time connection statistics

The system SHALL show active WebSocket and HTTP connections.

**Priority:** Low  
**Status:** Planned

#### Scenario: Count active WebSocket sessions

**Given** multiple users are connected via WebSocket  
**When** viewing connection metrics  
**Then** the dashboard shows "Active WebSocket connections: N"  
**And** the count updates when users connect or disconnect  
**And** the metric is streamed via SSE

#### Scenario: Track HTTP connection pool

**Given** the R2DBC connection pool is configured  
**When** connections are acquired and released  
**Then** active connection count is tracked  
**And** idle connection count is tracked  
**And** connection pool usage percentage is calculated and displayed

## Technical Notes

- **Metrics collection:** Spring Boot Actuator + Micrometer with built-in registries
- **REST endpoint:** `MetricsController.getMetricsSnapshot()` returns `Mono<MetricsSnapshot>`
- **SSE endpoint:** `MetricsController.streamMetrics()` returns `Flux<MetricUpdate>` with `TEXT_EVENT_STREAM`
- **Frontend:** TanStack Query for initial fetch, EventSource for SSE stream
- **Charts:** Recharts with `LineChart`, `BarChart`, and custom gauge component
- **Update frequency:** SSE emits every 2 seconds to balance freshness and overhead
- **Data retention:** Keep last 60 data points (2 minutes of history) in frontend state
