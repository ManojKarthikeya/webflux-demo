# Implementation Tasks

This document outlines the sequential tasks for implementing the reactive programming demo. Tasks are ordered to deliver incremental, verifiable progress with early visible results.

## Phase 1: Backend Foundation (Days 1-2)

### Task 1: Initialize Spring Boot backend project
- [x] Create `backend/` directory with Spring Initializr or manual setup
- [x] Configure `pom.xml` with dependencies:
  - `spring-boot-starter-webflux`
  - `spring-boot-starter-data-r2dbc`
  - `r2dbc-postgresql`
  - `spring-boot-starter-websocket`
  - `spring-boot-starter-actuator`
  - `micrometer-registry-prometheus` (optional)
- [x] Set Java version to 21
- [x] Create `ReactiveApplication.java` main class
- [x] Verify application starts on port 8080

**Validation:** Run `./mvnw spring-boot:run` and see application logs without errors

### Task 2: Configure PostgreSQL and R2DBC
- [x] Create `src/main/resources/application.yml`
- [x] Configure R2DBC connection URL, username, password
- [x] Configure connection pool settings (max size: 20)
- [x] Create `schema.sql` with table definitions:
  - `chat_messages`
  - `stock_transactions`
  - `users`
- [x] Create `R2dbcConfig.java` if custom configuration needed

**Validation:** Application connects to PostgreSQL on startup without errors

### Task 3: Create domain models and repositories
- [x] Create `model/ChatMessage.java` with `@Table` annotation
- [x] Create `model/StockTransaction.java`
- [x] Create `model/User.java`
- [x] Create `model/StockPrice.java` (not persisted, used for streaming)
- [x] Create `repository/ChatMessageRepository extends ReactiveCrudRepository`
- [x] Create `repository/StockTransactionRepository`
- [x] Create `repository/UserRepository`

**Validation:** Repositories are injectable and basic CRUD operations compile

### Task 4: Configure CORS for frontend
- [x] Create `config/CorsConfig.java`
- [x] Allow `http://localhost:5173` origin (Vite default)
- [x] Allow credentials and common headers
- [x] Apply CORS configuration to WebFlux

**Validation:** CORS headers present in OPTIONS responses

## Phase 2: Stock Dashboard Backend (Day 3)

### Task 5: Implement stock price streaming service
- [ ] Create `service/StockPriceService.java`
- [ ] Implement `generatePriceStream()` returning `Flux<List<StockPrice>>`
- [ ] Use `Flux.interval(Duration.ofMillis(500))` for periodic emission
- [ ] Track 5 stock symbols: AAPL, GOOGL, MSFT, AMZN, TSLA
- [ ] Simulate price changes with random ±2% fluctuation
- [ ] Include timestamp, symbol, price, and change percentage in each emission

**Validation:** Subscribe to Flux in a test and verify emissions every 500ms

### Task 6: Create stock streaming controller with SSE
- [ ] Create `controller/StockController.java`
- [ ] Implement `@GetMapping("/api/stocks/stream")` with `produces = TEXT_EVENT_STREAM_VALUE`
- [ ] Return `Flux<StockPrice>` from service
- [ ] Add `@CrossOrigin` if CORS config not global

**Validation:** `curl http://localhost:8080/api/stocks/stream` shows SSE data stream

### Task 7: Implement portfolio endpoints
- [ ] Create `service/PortfolioService.java`
- [ ] Implement `getTransactions(userId)` returning `Flux<StockTransaction>`
- [ ] Query repository with R2DBC: `findByUserIdOrderByCreatedAtDesc()`
- [ ] Add `@GetMapping("/api/portfolio/transactions")` in controller
- [ ] Return reactive stream of transactions

**Validation:** GET request returns transaction JSON array (empty if no data)

## Phase 3: Chat Backend (Days 4-5)

### Task 8: Configure WebSocket with STOMP
- [x] Create `config/WebSocketConfig.java` implementing `WebSocketMessageBrokerConfigurer`
- [x] Register STOMP endpoint `/ws` with SockJS fallback
- [x] Enable simple in-memory message broker with `/topic` prefix
- [x] Set application destination prefix to `/app`
- [x] Configure allowed origins for WebSocket

**Validation:** WebSocket endpoint accessible at `ws://localhost:8080/ws`

### Task 9: Implement chat message service and persistence
- [x] Create `service/ChatMessageService.java`
- [x] Implement `saveMessage(ChatMessage)` returning `Mono<ChatMessage>`
- [x] Use R2DBC repository to persist message
- [x] Implement `getRecentMessages(roomId, limit)` returning `Flux<ChatMessage>`
- [x] Query last N messages ordered by timestamp descending

**Validation:** Call service methods in test and verify database persistence

### Task 10: Create chat controller with message mapping
- [x] Create `controller/ChatController.java`
- [x] Implement `@MessageMapping("/chat/{roomId}")` for incoming messages
- [x] Inject `SimpMessagingTemplate` for broadcasting
- [x] On message receive:
  - Save to database via service (subscribe to Mono)
  - After save, broadcast to `/topic/chat/{roomId}`
- [x] Add `@GetMapping("/api/chat/{roomId}/history")` for message history
- [x] Return `Flux<ChatMessage>` from service

**Validation:** Send test message via WebSocket client and verify broadcast

### Task 11: Implement user presence tracking
- [x] Create `@MessageMapping("/chat/join")` handler
- [x] Track session ID to username mapping in ConcurrentHashMap
- [x] Broadcast join event to `/topic/presence/{roomId}`
- [x] Implement `@EventListener(SessionDisconnectEvent)` to detect disconnects
- [x] Broadcast leave event on disconnect
- [x] Add `/api/chat/{roomId}/users` endpoint returning current user list

**Validation:** Connect/disconnect clients and verify presence events

## Phase 4: Metrics Backend (Day 6)

### Task 12: Configure Micrometer and Actuator
- [x] Verify `spring-boot-starter-actuator` in dependencies
- [x] Configure `application.yml` to expose metrics endpoint
- [x] Enable JVM metrics, HTTP server metrics, and custom metrics
- [x] (Optional) Add Prometheus registry for future monitoring

**Validation:** GET `/actuator/metrics` returns list of available metrics

### Task 13: Create metrics aggregation service
- [x] Create `service/MetricsService.java`
- [x] Inject `MeterRegistry` from Micrometer
- [x] Implement `getCurrentMetrics()` returning `Mono<MetricsSnapshot>`
- [x] Use `Mono.zip()` to aggregate:
  - JVM memory usage (heap, non-heap)
  - Thread count (active, peak)
  - HTTP request count and rate
  - Average response time
- [x] Create `model/MetricsSnapshot.java` with all metric fields

**Validation:** Call service method and verify non-null metric values

### Task 14: Implement metrics REST and SSE endpoints
- [x] Create `controller/MetricsController.java`
- [x] Implement `@GetMapping("/api/metrics")` returning `Mono<MetricsSnapshot>`
- [x] Implement `@GetMapping("/api/metrics/stream")` with SSE
- [x] Use `Flux.interval(Duration.ofSeconds(2))` to emit metric updates
- [x] Map interval to metric snapshot collection
- [x] Return `Flux<MetricsSnapshot>` with `TEXT_EVENT_STREAM`

**Validation:** 
- GET `/api/metrics` returns JSON snapshot
- Stream endpoint emits events every 2 seconds

### Task 15: Add database query metrics
- [x] Create aspect or interceptor for repository methods
- [x] Use `@Timed` annotation on repository methods
- [x] Record query execution time in Micrometer timer
- [x] Tag metrics with operation type (findAll, save, etc.)
- [x] Include database metrics in MetricsSnapshot

**Validation:** Execute queries and verify timers in `/actuator/metrics`

## Phase 5: Frontend Foundation (Day 7)

### Task 16: Initialize React frontend project
- [x] Create `frontend/` directory
- [x] Initialize Vite project: `npm create vite@latest . -- --template react-ts`
- [x] Install dependencies:
  - `@tanstack/react-query`
  - `@mui/material @mui/icons-material @emotion/react @emotion/styled`
  - `recharts`
  - `@stomp/stompjs sockjs-client`
  - `@types/sockjs-client`
- [x] Configure `vite.config.ts` with proxy to backend (optional)
- [x] Create `src/api/client.ts` with base API configuration

**Validation:** `npm run dev` starts development server on port 5173

### Task 17: Set up routing and layout
- [x] Install `react-router-dom`
- [x] Create `src/components/Layout.tsx` with AppBar and navigation
- [x] Create `src/components/Navigation.tsx` with links to three pages
- [x] Configure routes in `App.tsx`:
  - `/` → Stock Dashboard
  - `/chat` → Chat Room
  - `/metrics` → Metrics Dashboard
- [x] Wrap app with `QueryClientProvider` from React Query

**Validation:** Navigate between pages and verify layout renders

### Task 18: Create TypeScript types
- [x] Create `src/types/stock.ts` with `StockPrice`, `StockTransaction`, `Portfolio` types
- [x] Create `src/types/chat.ts` with `ChatMessage`, `PresenceEvent` types
- [x] Create `src/types/metrics.ts` with `MetricsSnapshot` type
- [x] Export all types from `src/types/index.ts`

**Validation:** Types are importable and provide autocomplete

## Phase 6: Stock Dashboard Frontend (Days 8-9)

### Task 19: Create SSE hook for stock prices
- [ ] Create `src/hooks/useStockStream.ts`
- [ ] Use `useEffect` to create EventSource connection
- [ ] Listen to message events and parse JSON
- [ ] Update state with new price data
- [ ] Return `{ stocks, isConnected, error }` from hook
- [ ] Clean up EventSource on unmount

**Validation:** Hook connects to SSE endpoint and receives price updates

### Task 20: Implement stock dashboard page
- [ ] Create `src/pages/StockDashboard.tsx`
- [ ] Use `useStockStream()` hook to get live prices
- [ ] Display connection status indicator
- [ ] Create table/cards showing each stock with current price and change
- [ ] Color-code changes (green for positive, red for negative)
- [ ] Show last update timestamp

**Validation:** Page displays and updates stock prices every 500ms

### Task 21: Add price chart with live updates
- [ ] Create `src/components/shared/LiveChart.tsx` using Recharts
- [ ] Use `LineChart` with `XAxis` (time), `YAxis` (price)
- [ ] Maintain rolling window of 100 data points in state
- [ ] Update chart when new prices arrive via SSE
- [ ] Add smooth animation with `isAnimationActive` and `animationDuration`
- [ ] Integrate chart into StockDashboard page

**Validation:** Chart renders and updates smoothly with new data points

### Task 22: Implement portfolio display
- [ ] Fetch initial transactions with `useQuery` from React Query
- [ ] Calculate position values based on current prices from SSE
- [ ] Display positions in table with columns: symbol, quantity, cost, value, gain/loss
- [ ] Calculate and display total portfolio value
- [ ] Update portfolio value reactively when prices change

**Validation:** Portfolio values update in sync with price changes

## Phase 7: Chat Frontend (Days 10-11)

### Task 23: Create WebSocket hook with STOMP
- [x] Create `src/hooks/useChatWebSocket.ts`
- [x] Initialize STOMP client over SockJS
- [x] Implement `connect()`, `disconnect()`, `subscribe()`, `sendMessage()` functions
- [x] Handle connection state (connecting, connected, disconnected)
- [x] Implement auto-reconnect with exponential backoff
- [x] Return connection state and messaging functions

**Validation:** Hook connects to WebSocket and receives STOMP frames

### Task 24: Fetch chat history with React Query
- [x] Create `src/api/chat.ts` with `fetchChatHistory(roomId)` function
- [x] Use `useQuery` in chat component to fetch history on mount
- [x] Display loading state while fetching
- [x] Handle empty history with placeholder message

**Validation:** Chat history loads and displays on page mount

### Task 25: Implement chat room page
- [x] Create `src/pages/ChatRoom.tsx`
- [x] Use `useChatWebSocket()` hook for WebSocket connection
- [x] Subscribe to `/topic/chat/general` on connection
- [x] Display messages in scrollable list (newest at bottom)
- [x] Create message input form
- [x] Send messages to `/app/chat/general` on form submit
- [x] Append new messages to list when received via WebSocket
- [x] Auto-scroll to bottom when new message arrives

**Validation:** Messages sent appear in all connected clients

### Task 26: Add user presence indicators
- [x] Subscribe to `/topic/presence/general` in WebSocket hook
- [x] Maintain user list state from presence events
- [x] Display "X users online" in chat header
- [x] Optionally show user list sidebar
- [x] Send join notification on component mount
- [x] Handle leave events to remove users from list

**Validation:** User count updates when clients connect/disconnect

### Task 27: Polish chat UI
- [x] Style messages with sender name and timestamp
- [x] Differentiate own messages from others (alignment, color)
- [x] Add connection status indicator
- [x] Show "Connecting...", "Connected", "Disconnected" states
- [x] Disable input when disconnected
- [x] Show retry countdown during reconnection

**Validation:** Chat UI is visually clear and responsive

## Phase 8: Metrics Dashboard Frontend (Day 12)

### Task 28: Fetch initial metrics with React Query
- [x] Create `src/api/metrics.ts` with `fetchMetrics()` function
- [x] Use `useQuery` to fetch initial metrics snapshot
- [x] Display metrics in cards/grid layout
- [x] Show loading skeleton while fetching

**Validation:** Initial metrics load and display on page mount

### Task 29: Create SSE hook for metric updates
- [x] Create `src/hooks/useMetricsStream.ts`
- [x] Connect to `/api/metrics/stream` with EventSource
- [x] Parse incoming metric updates
- [x] Maintain rolling window of metric history (60 points)
- [x] Return `{ currentMetrics, history, isConnected }`

**Validation:** Hook receives metric updates every 2 seconds

### Task 30: Implement metrics dashboard page
- [x] Create `src/pages/MetricsDashboard.tsx`
- [x] Use `useQuery` for initial load and `useMetricsStream` for updates
- [x] Create grid layout with metric cards:
  - Memory usage card
  - CPU usage card (if available)
  - Thread count card
  - Request throughput card
- [x] Display current values with units

**Validation:** Metrics display and update in real-time

### Task 31: Add metrics charts
- [x] Create memory usage chart (line chart, heap vs non-heap)
- [x] Create request throughput chart (bar chart, req/s over time)
- [x] Create thread count gauge (custom or using Recharts PieChart)
- [x] Integrate charts into metrics page
- [x] Ensure charts update smoothly with new data

**Validation:** All charts render and animate with live data

### Task 32: Add database performance section
- [x] Display average query time metric
- [x] Show p95 and p99 percentiles
- [x] Display query count by operation type
- [x] Highlight slow queries (if metric > threshold)
- [x] Use color coding for performance (green, yellow, red)

**Validation:** Database metrics section displays correctly

## Phase 9: Polish and Documentation (Day 13)

### Task 33: Improve error handling
- [ ] Add error boundaries in React components
- [ ] Show user-friendly error messages for connection failures
- [ ] Add retry buttons for failed REST requests
- [ ] Log errors to console for debugging
- [ ] Handle SSE and WebSocket errors gracefully

**Validation:** Disconnect backend and verify error messages appear

### Task 34: Add loading states and skeletons
- [ ] Add skeleton loaders for initial data fetches
- [ ] Show loading spinners for async operations
- [ ] Display "Connecting..." states for WebSocket and SSE
- [ ] Ensure UI is never completely blank during loading

**Validation:** Page transitions show loading states appropriately

### Task 35: Create root README
- [ ] Create `README.md` in project root
- [ ] Document project purpose and features
- [ ] Add prerequisites (Java 21, Node.js 18+, PostgreSQL)
- [ ] Provide setup instructions:
  - Database setup (create database, run schema.sql)
  - Backend setup (./mvnw spring-boot:run)
  - Frontend setup (npm install && npm run dev)
- [ ] Add screenshots or GIF demos (optional)
- [ ] Include troubleshooting section

**Validation:** Follow README from scratch and verify app starts

### Task 36: Create backend README
- [ ] Create `backend/README.md`
- [ ] Document project structure (packages)
- [ ] Explain reactive patterns used
- [ ] Document API endpoints:
  - REST endpoints with request/response examples
  - SSE endpoints with event format
  - WebSocket endpoints with STOMP destinations
- [ ] Add configuration options (application.yml)

**Validation:** README accurately describes backend implementation

### Task 37: Create frontend README
- [ ] Create `frontend/README.md`
- [ ] Document project structure (src folders)
- [ ] Explain component architecture
- [ ] Document hooks and their usage
- [ ] List available scripts (dev, build, preview)
- [ ] Add notes on WebSocket and SSE integration

**Validation:** README helps new developers understand frontend code

### Task 38: Final testing and validation
- [ ] Test all three pages with backend running
- [ ] Verify SSE reconnects after backend restart
- [ ] Verify WebSocket reconnects with exponential backoff
- [ ] Test with multiple browser windows (chat message delivery)
- [ ] Verify database queries are non-blocking (check logs)
- [ ] Test error scenarios (DB down, invalid data)
- [ ] Verify CORS allows frontend requests
- [ ] Check browser console for errors

**Validation:** All features work end-to-end without errors

## Dependency Notes

- **Parallelizable work:**
  - Tasks 5-7 (stock backend) can overlap with Tasks 16-18 (frontend setup)
  - Tasks 19-22 (stock frontend) can start once Task 5 is complete
  - Tasks 23-27 (chat frontend) can start once Task 10 is complete
  - Tasks 28-32 (metrics frontend) can start once Task 14 is complete

- **Sequential dependencies:**
  - Task 2 must complete before Task 3 (database must exist before models)
  - Task 8 must complete before Task 10 (WebSocket config needed for controller)
  - Task 13 must complete before Task 14 (service needed for controller)
  - Tasks 35-37 should be done after implementation is complete

## Testing Strategy

Each task includes a validation step. Additional testing:
- Manual testing via browser and curl
- Optional: Add unit tests for services using StepVerifier (Reactor test library)
- Optional: Integration tests for controllers using WebTestClient

## Estimated Timeline

- **Phase 1-2:** 3 days (backend foundation + stock)
- **Phase 3-4:** 3 days (chat + metrics backend)
- **Phase 5-6:** 3 days (frontend setup + stock UI)
- **Phase 7:** 2 days (chat UI)
- **Phase 8:** 1 day (metrics UI)
- **Phase 9:** 1 day (polish + docs)

**Total: ~13 days** of focused development
