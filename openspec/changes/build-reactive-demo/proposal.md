# Proposal: Build Reactive Programming Demo

**Change ID:** `build-reactive-demo`  
**Status:** Draft  
**Created:** 2025-12-15

## Summary

Build a full-stack reactive programming demonstration featuring Spring WebFlux backend and React with TanStack Query frontend. The demo will consist of three pages showcasing different reactive patterns: real-time stock trading dashboard, collaborative chat application, and live system metrics monitoring.

## Problem Statement

Currently, the repository lacks a concrete demonstration of reactive programming capabilities. Developers need a working reference implementation that showcases:

1. End-to-end reactive data flow from database to UI
2. Real-time bidirectional communication using WebSocket
3. Server-sent events for unidirectional streaming
4. Non-blocking reactive database access with R2DBC
5. Reactive event streaming with proper backpressure handling

## Proposed Solution

Create a three-page demo application with separate `backend/` and `frontend/` directories:

### Page 1: Stock Trading Dashboard
- **Purpose:** Demonstrate SSE (Server-Sent Events) for unidirectional streaming
- **Features:**
  - Live stock price updates pushed from server
  - Real-time portfolio value calculation
  - Historical price chart with live updates
  - WebFlux streaming endpoint with `Flux<StockPrice>`

### Page 2: Collaborative Chat
- **Purpose:** Demonstrate WebSocket for bidirectional communication
- **Features:**
  - Multi-user chat rooms
  - Real-time message delivery
  - User presence indicators (online/offline)
  - Message persistence with R2DBC PostgreSQL
  - STOMP protocol over WebSocket

### Page 3: System Metrics Dashboard
- **Purpose:** Demonstrate reactive database queries and combined streaming patterns
- **Features:**
  - Live JVM metrics (memory, CPU, threads)
  - Request throughput and latency charts
  - Active connections count
  - Database query performance metrics
  - Combines REST queries with SSE streams

### Architecture

**Backend (Spring WebFlux):**
- Java 21 with Spring Boot 3.x
- Spring WebFlux on Netty (non-blocking server)
- R2DBC with PostgreSQL for reactive database access
- WebSocket with STOMP for bidirectional messaging
- SSE endpoints for unidirectional streaming
- Micrometer for metrics collection
- Project Reactor (`Mono`, `Flux`) throughout

**Frontend (React + TypeScript):**
- React 18 with TypeScript
- TanStack Query (React Query) for REST API calls
- `@stomp/stompjs` + `sockjs-client` for WebSocket
- Native `EventSource` API for SSE
- Recharts for live data visualization
- MUI (Material-UI) component library
- Vite for build tooling

**Data Layer:**
- PostgreSQL with R2DBC driver
- In-memory stock price simulation (no external APIs needed for demo)
- Redis optional for caching (can be added later)

## Capabilities Affected

This change introduces the following new capabilities:

1. **stock-trading-dashboard** - Real-time stock price streaming and portfolio management
2. **collaborative-chat** - Multi-user chat with WebSocket communication
3. **system-metrics-dashboard** - Live system monitoring and performance metrics
4. **reactive-backend-core** - Spring WebFlux backend infrastructure
5. **reactive-frontend-core** - React + TanStack Query frontend infrastructure

## Success Criteria

- [ ] All three pages are functional and accessible from navigation
- [ ] Stock prices update in real-time without manual refresh
- [ ] Chat messages are delivered instantly to all connected users
- [ ] System metrics refresh continuously with smooth animations
- [ ] Backend uses only non-blocking reactive types (`Mono`, `Flux`)
- [ ] Frontend uses TanStack Query for REST and native APIs for streaming
- [ ] All database operations use R2DBC (no JDBC)
- [ ] WebSocket connection handles reconnection gracefully
- [ ] Application starts with single command for backend and frontend
- [ ] README includes setup and run instructions

## Out of Scope

- User authentication and authorization (users can be simulated)
- Kafka integration (can be added in future iteration)
- Production deployment configuration
- Horizontal scaling and load balancing
- Comprehensive error handling and retry policies
- Unit and integration tests (focus on demonstration)
- CI/CD pipeline
- Docker containerization

## Dependencies

- Java 21 JDK
- Node.js 18+ and npm/yarn
- PostgreSQL 14+
- Modern web browser with WebSocket and SSE support

## Risks and Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| WebSocket connection stability | Medium | Implement auto-reconnect with exponential backoff |
| Database connection pool exhaustion | Low | Configure R2DBC connection pool limits |
| Frontend performance with rapid updates | Medium | Implement throttling and windowing for chart data |
| Browser compatibility for SSE | Low | Check EventSource support; provide fallback message |

## Alternatives Considered

1. **Single-page application:** Rejected because three distinct pages better demonstrate different reactive patterns
2. **MongoDB instead of PostgreSQL:** Rejected because R2DBC + PostgreSQL is more common in enterprise
3. **GraphQL subscriptions:** Deferred to future iteration; WebSocket + REST is simpler starting point
4. **Kafka for event streaming:** Deferred to keep demo setup simple
