# Reactive Programming Demo

Full-stack reactive programming demonstration featuring **Spring WebFlux** backend and **React + TanStack Query** frontend. This project showcases three different reactive patterns through interactive web applications.

## 🎯 Demo Features

### 1. **Stock Trading Dashboard** (Coming Soon)
- Real-time stock price updates via Server-Sent Events (SSE)
- Live portfolio value calculations
- Interactive price charts with Recharts
- Non-blocking reactive queries with R2DBC

### 2. **Collaborative Chat** ✅ (Implemented)
- Multi-user chat rooms with WebSocket + STOMP
- Real-time bidirectional messaging
- User presence indicators (online/offline tracking)
- Message persistence with reactive PostgreSQL
- Automatic reconnection with exponential backoff

### 3. **System Metrics Dashboard** (Coming Soon)
- Live JVM metrics (memory, CPU, threads)
- Request throughput and latency charts
- Database query performance monitoring
- Hybrid REST + SSE pattern

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend (React)                      │
│  TanStack Query • WebSocket • SSE • Recharts            │
└────────────────────┬────────────────────────────────────┘
                     │ HTTP/WS/SSE
┌────────────────────▼────────────────────────────────────┐
│              Backend (Spring WebFlux)                    │
│  Netty • Project Reactor • STOMP • Micrometer           │
└────────────────────┬────────────────────────────────────┘
                     │ R2DBC
┌────────────────────▼────────────────────────────────────┐
│                  PostgreSQL Database                     │
└─────────────────────────────────────────────────────────┘
```

## 🚀 Quick Start

### Prerequisites

- **Java 21 JDK** - [Download](https://adoptium.net/)
- **Node.js 18+** - [Download](https://nodejs.org/)
- **PostgreSQL 14+** - [Download](https://www.postgresql.org/download/)
- **Maven 3.8+** - [Download](https://maven.apache.org/download.cgi)

### Database Setup

```bash
# Create database
createdb reactive_demo

# Create user
psql -d postgres -c "CREATE USER demo_user WITH PASSWORD 'demo_pass';"
psql -d postgres -c "GRANT ALL PRIVILEGES ON DATABASE reactive_demo TO demo_user;"
```

### Backend Setup

```bash
cd backend

# Build and run
mvn spring-boot:run

# Or use the build script
./build.sh
```

Backend runs on **http://localhost:8080**

### Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

Frontend runs on **http://localhost:5173**

## 📁 Project Structure

```
webflux-demo/
├── backend/                      # Spring Boot WebFlux backend
│   ├── src/main/java/com/demo/reactive/
│   │   ├── config/              # CORS, WebSocket configuration
│   │   ├── controller/          # REST & WebSocket controllers
│   │   ├── service/             # Business logic
│   │   ├── repository/          # R2DBC repositories
│   │   └── model/               # Domain entities
│   ├── src/main/resources/
│   │   ├── application.yml      # App configuration
│   │   └── schema.sql           # Database schema
│   └── pom.xml
│
├── frontend/                     # React + TypeScript frontend (Coming)
│   ├── src/
│   │   ├── api/                 # API client functions
│   │   ├── hooks/               # Custom React hooks
│   │   ├── components/          # React components
│   │   ├── pages/               # Page components
│   │   └── types/               # TypeScript types
│   └── package.json
│
└── openspec/                     # OpenSpec documentation
    ├── changes/build-reactive-demo/
    │   ├── proposal.md          # Change proposal
    │   ├── design.md            # Architecture decisions
    │   ├── tasks.md             # Implementation tasks
    │   └── specs/               # Capability specifications
    └── AGENTS.md
```

## 🔧 Technology Stack

### Backend
- **Spring Boot 3.2.1** - Application framework
- **Spring WebFlux** - Reactive web on Netty
- **Spring Data R2DBC** - Reactive database access
- **R2DBC PostgreSQL** - Non-blocking PostgreSQL driver
- **Spring WebSocket** - WebSocket with STOMP
- **Micrometer** - Application metrics
- **Lombok** - Boilerplate reduction

### Frontend (Coming Soon)
- **React 18** - UI framework
- **TypeScript** - Type safety
- **TanStack Query** - Data fetching & caching
- **Material-UI (MUI)** - Component library
- **Recharts** - Data visualization
- **@stomp/stompjs** - WebSocket client
- **Vite** - Build tool

### Database
- **PostgreSQL 14+** with R2DBC driver

## 🎓 Reactive Patterns Demonstrated

### 1. Non-blocking Database Access (R2DBC)

```java
public Mono<ChatMessage> saveMessage(ChatMessage message) {
    return chatMessageRepository.save(message);
}

public Flux<ChatMessage> getRecentMessages(String roomId, int limit) {
    return chatMessageRepository.findRecentMessagesByRoomId(roomId, limit);
}
```

### 2. WebSocket Bidirectional Messaging (STOMP)

```java
@MessageMapping("/chat/{roomId}")
public void handleChatMessage(@DestinationVariable String roomId, 
                              @Payload ChatMessage message) {
    chatMessageService.saveMessage(message)
        .subscribe(saved -> 
            messagingTemplate.convertAndSend("/topic/chat/" + roomId, saved)
        );
}
```

### 3. Server-Sent Events (SSE) Streaming

```java
@GetMapping(value = "/api/stocks/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<StockPrice> streamStockPrices() {
    return stockPriceService.generatePriceStream();
}
```

### 4. Event-Driven Architecture

```java
@EventListener
public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
    String sessionId = headerAccessor.getSessionId();
    presenceService.userLeft(sessionId);
}
```

## 📡 API Endpoints

### Chat REST Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/chat/{roomId}/history` | Get recent chat messages |
| GET | `/api/chat/{roomId}/users` | Get active users in room |
| GET | `/actuator/health` | Health check |
| GET | `/actuator/metrics` | Application metrics |

### WebSocket Endpoints

| Destination | Type | Description |
|-------------|------|-------------|
| `/ws` | Connect | WebSocket endpoint |
| `/app/chat/join/{roomId}` | SEND | Join a chat room |
| `/app/chat/{roomId}` | SEND | Send message to room |
| `/topic/chat/{roomId}` | SUBSCRIBE | Receive room messages |
| `/topic/presence/{roomId}` | SUBSCRIBE | Receive presence updates |

## 🧪 Testing the Chat Feature

1. **Start Backend & Database:**
   ```bash
   # Terminal 1: Start PostgreSQL (if not running)
   brew services start postgresql
   
   # Terminal 2: Run backend
   cd backend && mvn spring-boot:run
   ```

2. **Test WebSocket Connection:**
   Use a WebSocket client like [Postman](https://www.postman.com/) or browser console:
   
   ```javascript
   const socket = new SockJS('http://localhost:8080/ws');
   const stompClient = Stomp.over(socket);
   
   stompClient.connect({}, () => {
       // Subscribe to room
       stompClient.subscribe('/topic/chat/general', (message) => {
           console.log('Received:', JSON.parse(message.body));
       });
       
       // Send message
       stompClient.send('/app/chat/general', {}, JSON.stringify({
           userName: 'John',
           messageText: 'Hello!'
       }));
   });
   ```

3. **Test REST API:**
   ```bash
   # Get chat history
   curl http://localhost:8080/api/chat/general/history?limit=10
   
   # Get active users
   curl http://localhost:8080/api/chat/general/users
   ```

## 📚 Documentation

- [Backend README](backend/README.md) - Detailed backend documentation
- [Frontend README](frontend/README.md) - Frontend documentation (Coming)
- [OpenSpec Proposal](openspec/changes/build-reactive-demo/proposal.md) - Full proposal
- [Design Document](openspec/changes/build-reactive-demo/design.md) - Architecture decisions
- [Task List](openspec/changes/build-reactive-demo/tasks.md) - Implementation checklist

## 🐛 Troubleshooting

**Database connection errors:**
```bash
# Check PostgreSQL is running
brew services list | grep postgresql

# Test connection
psql -U demo_user -d reactive_demo -h localhost
```

**Port conflicts:**
- Backend: Change port in `backend/src/main/resources/application.yml`
- Frontend: Set `PORT=3001` environment variable

**WebSocket issues:**
- Verify CORS settings in `WebSocketConfig.java`
- Check browser console for connection errors
- Ensure SockJS fallback is enabled

## 📈 Current Implementation Status

- ✅ **Backend Foundation** - Complete
  - Spring Boot WebFlux setup
  - R2DBC PostgreSQL configuration
  - Domain models and repositories
  - CORS configuration

- ✅ **Collaborative Chat** - Complete
  - WebSocket with STOMP
  - Message persistence
  - User presence tracking
  - REST API for history

- ⏳ **Stock Dashboard** - Pending
- ⏳ **Metrics Dashboard** - Pending
- ⏳ **Frontend Application** - Pending

## 🤝 Contributing

This is a demonstration project following OpenSpec methodology. To propose changes:

1. Review [openspec/AGENTS.md](openspec/AGENTS.md)
2. Create a change proposal in `openspec/changes/`
3. Implement following the task list
4. Update documentation

## 📄 License

This project is for educational and demonstration purposes.

## 🔗 Links

- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Project Reactor](https://projectreactor.io/)
- [R2DBC](https://r2dbc.io/)
- [React Query (TanStack Query)](https://tanstack.com/query/latest)
- [OpenSpec](https://github.com/openspec-ai/openspec)
