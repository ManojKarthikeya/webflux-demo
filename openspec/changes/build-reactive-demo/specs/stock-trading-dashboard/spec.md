# Capability: Stock Trading Dashboard

Real-time stock price streaming and portfolio management using Server-Sent Events (SSE) for unidirectional data flow from backend to frontend.

## ADDED Requirements

### Requirement: Stream live stock prices via SSE

The system SHALL stream real-time stock price updates from backend to frontend using Server-Sent Events.

**Priority:** High  
**Status:** Planned

#### Scenario: Client receives continuous price updates

**Given** a client connects to the SSE endpoint `/api/stocks/stream`  
**When** the backend generates price updates every 500ms  
**Then** the client receives stock price events containing symbol, current price, change percentage, and timestamp  
**And** the stream remains open until the client disconnects

#### Scenario: Backend emits multiple stock symbols

**Given** the backend is tracking 5 stock symbols (AAPL, GOOGL, MSFT, AMZN, TSLA)  
**When** prices are updated  
**Then** each emission contains all 5 stock prices  
**And** prices fluctuate randomly within ±2% of previous value

#### Scenario: Client reconnects after disconnect

**Given** a client was receiving stock price streams  
**When** the connection is lost  
**Then** the browser's EventSource automatically reconnects  
**And** price streaming resumes without manual intervention

### Requirement: Display stock price chart with live updates

The frontend SHALL render an interactive line chart showing price trends over time with automatic updates.

**Priority:** High  
**Status:** Planned

#### Scenario: Chart updates without full re-render

**Given** a chart is displaying historical prices  
**When** new price data arrives via SSE  
**Then** the chart smoothly appends the new data point  
**And** the time axis scrolls to keep recent data visible  
**And** animation duration is 300ms for smooth transition

#### Scenario: Chart maintains rolling window

**Given** the chart has accumulated 100 data points  
**When** a new data point arrives  
**Then** the oldest data point is removed  
**And** the chart displays only the most recent 100 points  
**And** memory usage remains constant

### Requirement: Calculate portfolio value in real-time

The system SHALL calculate and update total portfolio value based on current prices and holdings.

**Priority:** Medium  
**Status:** Planned

#### Scenario: Portfolio updates with price changes

**Given** a user holds 10 AAPL shares at $150 and 5 GOOGL shares at $120  
**When** AAPL price updates to $152  
**Then** the AAPL position value updates to $1,520  
**And** total portfolio value updates to $2,120  
**And** the change is highlighted with color (green for gain, red for loss)

#### Scenario: Display individual stock positions

**Given** a user has multiple stock positions  
**When** viewing the dashboard  
**Then** each position shows symbol, quantity, average cost, current price, and unrealized gain/loss  
**And** positions are sorted by absolute gain/loss descending

### Requirement: Provide portfolio transaction history via reactive query

The backend SHALL retrieve transaction history using R2DBC reactive queries.

**Priority:** Low  
**Status:** Planned

#### Scenario: Fetch user transactions reactively

**Given** a user has recorded 20 stock transactions  
**When** the frontend requests `/api/portfolio/transactions`  
**Then** the backend queries R2DBC repository returning `Flux<StockTransaction>`  
**And** transactions are returned in reverse chronological order (newest first)  
**And** the response streams results as they are fetched from database

#### Scenario: Handle empty transaction history

**Given** a user has no transactions  
**When** requesting transaction history  
**Then** the backend returns an empty `Flux`  
**And** the frontend displays "No transactions yet" message

## Technical Notes

- **Backend:** Spring WebFlux controller with `@GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)`
- **Service:** `StockPriceService` using `Flux.interval()` to simulate price updates
- **Frontend:** React `useEffect` with `EventSource` API
- **Chart library:** Recharts `LineChart` component
- **Data structure:** `{ symbol: string, price: number, change: number, timestamp: number }`
