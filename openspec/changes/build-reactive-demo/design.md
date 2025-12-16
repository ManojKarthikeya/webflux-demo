# Design: Build Reactive Programming Demo

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                         Browser                              │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │  Stock      │  │  Chat        │  │  Metrics     │       │
│  │  Dashboard  │  │  Room        │  │  Dashboard   │       │
│  └──────┬──────┘  └──────┬───────┘  └──────┬───────┘       │
│         │                 │                  │               │
│    EventSource      WebSocket (STOMP)   REST + SSE          │
└─────────┼─────────────────┼──────────────────┼──────────────┘
          │                 │                  │
          ▼                 ▼                  ▼
┌─────────────────────────────────────────────────────────────┐
│              Spring WebFlux Backend (Netty)                  │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │                     Controllers                         │ │
│  │  - StockController (SSE /api/stocks/stream)            │ │
│  │  - ChatController (WS /ws + STOMP /topic/chat)         │ │
│  │  - MetricsController (REST + SSE /api/metrics/stream)  │ │
│  └────────────────┬───────────────────────────────────────┘ │
│                   │                                          │
│  ┌────────────────▼───────────────────────────────────────┐ │
│  │                   Service Layer                         │ │
│  │  - StockPriceService (Flux<StockPrice>)                │ │
│  │  - ChatMessageService (Mono/Flux)                      │ │
│  │  - MetricsService (Micrometer integration)             │ │
│  └────────────────┬───────────────────────────────────────┘ │
│                   │                                          │
│  ┌────────────────▼───────────────────────────────────────┐ │
│  │              Repository Layer (R2DBC)                   │ │
│  │  - ChatMessageRepository (ReactiveCrudRepository)      │ │
│  │  - UserRepository                                      │ │
│  │  - StockTransactionRepository                          │ │
│  └────────────────┬───────────────────────────────────────┘ │
└───────────────────┼──────────────────────────────────────────┘
                    │
                    ▼
          ┌─────────────────┐
          │   PostgreSQL    │
          │   (R2DBC Pool)  │
          └─────────────────┘
```

## Technology Decisions

### Backend Stack

#### Why Spring WebFlux?
- **Non-blocking I/O:** Netty-based server handles high concurrency with fewer threads
- **Reactive types:** `Mono` and `Flux` provide composable async operations with backpressure
- **Ecosystem:** Rich integration with R2DBC, WebSocket, metrics, and Spring Boot
- **Industry standard:** Widely adopted for reactive Java applications

#### Why R2DBC with PostgreSQL?
- **Reactive driver:** True non-blocking database access (no thread blocking)
- **Backpressure support:** Database queries respect reactive streams backpressure
- **PostgreSQL:** Mature, reliable, enterprise-grade relational database
- **Familiar:** SQL and relational model familiar to most developers

**Alternative considered:** MongoDB with reactive driver
- **Rejected because:** PostgreSQL + R2DBC better demonstrates reactive relational data access
- **Use case fit:** Document store less relevant for demo scenarios

#### Why WebSocket with STOMP?
- **Bidirectional:** Chat requires client-to-server and server-to-client messaging
- **STOMP protocol:** Higher-level abstraction over raw WebSocket; easier to work with
- **Spring support:** Excellent integration with Spring WebFlux message broker
- **Pub/Sub model:** Topic-based subscriptions ideal for chat rooms

#### Why SSE for Stock Prices?
- **Unidirectional:** Stock prices flow server → client only
- **Native browser API:** `EventSource` built into browsers, no library needed
- **Simpler than WebSocket:** Less overhead for one-way streaming
- **Auto-reconnect:** Browser handles reconnection automatically

**Alternative considered:** WebSocket for everything
- **Rejected because:** SSE simpler for unidirectional streams; demo showcases both patterns

### Frontend Stack

#### Why React with TypeScript?
- **Component model:** Ideal for building interactive UIs with state management
- **TypeScript:** Type safety prevents runtime errors and improves developer experience
- **Ecosystem:** Massive library ecosystem for charts, WebSocket clients, etc.
- **Industry standard:** Most popular frontend framework

#### Why TanStack Query?
- **Declarative data fetching:** `useQuery` hook simplifies REST API calls
- **Caching & sync:** Automatic background refetch and cache management
- **Loading states:** Built-in loading, error, and success states
- **Optimistic updates:** Can update UI before server confirms (for chat)

**Alternative considered:** Redux Toolkit with RTK Query
- **Rejected because:** TanStack Query simpler for demo; Redux adds unnecessary complexity

#### Why MUI (Material-UI)?
- **Component library:** Pre-built components (AppBar, Card, TextField, etc.)
- **Professional look:** Modern design out of the box
- **TypeScript support:** Excellent TypeScript definitions
- **Customizable:** Easy to theme and customize

**Alternative considered:** Ant Design, Chakra UI
- **Rejected because:** MUI most popular and well-documented

#### Why Recharts?
- **React-friendly:** Built specifically for React with declarative API
- **Live data:** Handles dynamic data updates smoothly
- **Customizable:** Easy to style and configure
- **Lightweight:** Smaller bundle than Chart.js or ECharts

### Data Flow Patterns

#### Pattern 1: SSE Streaming (Stock Dashboard)

```
Frontend                Backend                   Service
   │                       │                         │
   │ EventSource connect   │                         │
   ├──────────────────────>│                         │
   │                       │ GET /api/stocks/stream  │
   │                       ├────────────────────────>│
   │                       │                         │
   │                       │  return Flux<StockPrice>│
   │                       │<────────────────────────┤
   │                       │                         │
   │  data: {symbol,price} │      emit every 500ms   │
   │<──────────────────────┤<────────────────────────┤
   │  data: {symbol,price} │                         │
   │<──────────────────────┤<────────────────────────┤
   │       ...             │          ...            │
```

**Key decisions:**
- Use `Flux.interval()` to simulate real-time price changes
- Use `SseEmitter` or `text/event-stream` media type
- Frontend: `EventSource` with message event handler
- Reconnect automatically on disconnect

#### Pattern 2: WebSocket Bidirectional (Chat)

```
Frontend                Backend                   Service           DB
   │                       │                         │              │
   │ STOMP connect /ws     │                         │              │
   ├──────────────────────>│                         │              │
   │                       │                         │              │
   │ SUBSCRIBE /topic/room1│                         │              │
   ├──────────────────────>│                         │              │
   │                       │                         │              │
   │ SEND /app/chat        │                         │              │
   ├──────────────────────>│ save message            │              │
   │                       ├────────────────────────>│  INSERT      │
   │                       │                         ├─────────────>│
   │                       │                         │  Mono<Msg>   │
   │                       │                         │<─────────────┤
   │                       │                         │              │
   │                       │ broadcast to /topic/room1              │
   │  MESSAGE /topic/room1 │                         │              │
   │<──────────────────────┤                         │              │
   │  MESSAGE /topic/room1 │  (all subscribers)      │              │
   │<──────────────────────┤                         │              │
```

**Key decisions:**
- Use `@MessageMapping` for incoming messages
- Use `SimpMessagingTemplate` to broadcast to topics
- Persist messages with R2DBC before broadcasting
- Frontend: `@stomp/stompjs` with subscription callbacks

#### Pattern 3: REST + SSE Hybrid (Metrics)

```
Frontend                Backend
   │                       │
   │ GET /api/metrics      │  Initial snapshot
   ├──────────────────────>│
   │  { cpu, memory, ... } │
   │<──────────────────────┤
   │                       │
   │ EventSource connect   │  Live updates stream
   ├──────────────────────>│
   │  /api/metrics/stream  │
   │                       │
   │  data: { cpu: 45% }   │
   │<──────────────────────┤
   │  data: { cpu: 47% }   │
   │<──────────────────────┤
```

**Key decisions:**
- REST endpoint for initial load (fast first paint)
- SSE for incremental updates (reduces data transfer)
- TanStack Query caches initial fetch
- EventSource updates trigger chart re-render

## Database Schema

### Chat Messages

```sql
CREATE TABLE chat_messages (
  id BIGSERIAL PRIMARY KEY,
  room_id VARCHAR(100) NOT NULL,
  user_name VARCHAR(100) NOT NULL,
  message_text TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  INDEX idx_room_created (room_id, created_at DESC)
);
```

### Stock Transactions (Optional - for portfolio)

```sql
CREATE TABLE stock_transactions (
  id BIGSERIAL PRIMARY KEY,
  user_id VARCHAR(100) NOT NULL,
  symbol VARCHAR(10) NOT NULL,
  quantity INTEGER NOT NULL,
  price_per_share DECIMAL(10, 2) NOT NULL,
  transaction_type VARCHAR(10) NOT NULL, -- 'BUY' or 'SELL'
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

### Users (Simple - no auth)

```sql
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(100) UNIQUE NOT NULL,
  display_name VARCHAR(200),
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

## Project Structure

```
/
├── backend/
│   ├── src/main/java/com/demo/reactive/
│   │   ├── ReactiveApplication.java
│   │   ├── config/
│   │   │   ├── R2dbcConfig.java
│   │   │   ├── WebSocketConfig.java
│   │   │   └── CorsConfig.java
│   │   ├── controller/
│   │   │   ├── StockController.java
│   │   │   ├── ChatController.java
│   │   │   └── MetricsController.java
│   │   ├── service/
│   │   │   ├── StockPriceService.java
│   │   │   ├── ChatMessageService.java
│   │   │   └── MetricsService.java
│   │   ├── repository/
│   │   │   ├── ChatMessageRepository.java
│   │   │   ├── UserRepository.java
│   │   │   └── StockTransactionRepository.java
│   │   └── model/
│   │       ├── ChatMessage.java
│   │       ├── StockPrice.java
│   │       ├── StockTransaction.java
│   │       └── User.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── schema.sql
│   ├── pom.xml
│   └── README.md
│
├── frontend/
│   ├── src/
│   │   ├── main.tsx
│   │   ├── App.tsx
│   │   ├── api/
│   │   │   ├── client.ts
│   │   │   ├── stocks.ts
│   │   │   ├── chat.ts
│   │   │   └── metrics.ts
│   │   ├── hooks/
│   │   │   ├── useStockStream.ts
│   │   │   ├── useChatWebSocket.ts
│   │   │   └── useMetricsStream.ts
│   │   ├── components/
│   │   │   ├── Layout.tsx
│   │   │   ├── Navigation.tsx
│   │   │   └── shared/
│   │   │       ├── LiveChart.tsx
│   │   │       └── ConnectionStatus.tsx
│   │   ├── pages/
│   │   │   ├── StockDashboard.tsx
│   │   │   ├── ChatRoom.tsx
│   │   │   └── MetricsDashboard.tsx
│   │   └── types/
│   │       ├── stock.ts
│   │       ├── chat.ts
│   │       └── metrics.ts
│   ├── package.json
│   ├── vite.config.ts
│   ├── tsconfig.json
│   └── README.md
│
└── README.md (root)
```

## Configuration

### Backend (application.yml)

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/reactive_demo
    username: demo_user
    password: demo_pass
  
server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info
```

### Frontend (environment)

```typescript
const config = {
  apiBaseUrl: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  wsUrl: import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws',
};
```

## Performance Considerations

### Backpressure Handling

- Use `Flux.buffer()` to batch database writes (chat messages)
- Apply `limitRate()` on SSE streams to control emission rate
- Use `window()` or `sample()` to reduce frontend updates

### Connection Limits

- Configure R2DBC connection pool: `maxSize: 20`
- Set WebSocket connection limit in application.yml
- Use connection pooling for PostgreSQL

### Memory Management

- Keep stock price history limited (rolling window of 100 points)
- Clear old chat messages with scheduled cleanup
- Use pagination for historical data queries

## Security Notes (Out of Scope but Noted)

While authentication is out of scope for this demo:
- CORS is configured to allow localhost:5173 (Vite default)
- WebSocket origins are restricted
- SQL injection prevented by parameterized queries (R2DBC)
- XSS prevention: React escapes by default

Future iterations should add:
- JWT-based authentication
- Rate limiting on WebSocket connections
- Input validation with Bean Validation
- HTTPS/WSS in production
