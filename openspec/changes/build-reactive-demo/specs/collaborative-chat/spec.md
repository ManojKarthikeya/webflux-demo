# Capability: Collaborative Chat

Multi-user chat application with real-time message delivery using WebSocket and STOMP protocol for bidirectional communication.

## ADDED Requirements

### Requirement: Establish WebSocket connection with STOMP

The system SHALL allow clients to connect via WebSocket using STOMP protocol for bidirectional messaging.

**Priority:** High  
**Status:** Planned

#### Scenario: Client connects to WebSocket endpoint

**Given** the backend WebSocket endpoint is available at `/ws`  
**When** a client initiates STOMP connection  
**Then** the connection is established successfully  
**And** the client receives a CONNECTED frame  
**And** the connection remains open for bidirectional communication

#### Scenario: Client subscribes to chat room topic

**Given** a WebSocket connection is established  
**When** the client subscribes to `/topic/chat/{roomId}`  
**Then** the subscription is confirmed  
**And** the client receives all messages published to that topic  
**And** messages from other subscribers are delivered in real-time

#### Scenario: Connection lost and reconnect

**Given** a client is connected via WebSocket  
**When** the network connection is lost  
**Then** the client detects disconnection  
**And** the client automatically attempts to reconnect with exponential backoff (1s, 2s, 4s, 8s, max 30s)  
**And** upon reconnection, the client re-subscribes to previous topics

### Requirement: Send and receive chat messages reactively

Users SHALL send messages through WebSocket that are persisted to database and broadcast to all room participants.

**Priority:** High  
**Status:** Planned

#### Scenario: User sends message to chat room

**Given** a user is connected to room "general"  
**When** the user sends message "Hello everyone" to `/app/chat/general`  
**Then** the backend receives the message via `@MessageMapping`  
**And** the message is saved to database using R2DBC (`Mono<ChatMessage>`)  
**And** after successful save, the message is broadcast to `/topic/chat/general`  
**And** all subscribed clients receive the message

#### Scenario: Multiple users receive message simultaneously

**Given** 3 users are subscribed to room "general"  
**When** user A sends a message  
**Then** users B and C receive the message within 100ms  
**And** user A also receives their own message as confirmation  
**And** the message includes sender name, text, room ID, and timestamp

#### Scenario: Message persistence with reactive database

**Given** a chat message is being processed  
**When** the service calls repository.save(message)  
**Then** R2DBC executes INSERT without blocking threads  
**And** the save operation returns `Mono<ChatMessage>` with generated ID  
**And** the returned Mono is subscribed and completed before broadcasting

### Requirement: Display user presence indicators

The frontend SHALL show which users are currently online in the chat room.

**Priority:** Medium  
**Status:** Planned

#### Scenario: User joins chat room

**Given** a user connects and subscribes to chat room  
**When** the user sends a join notification to `/app/chat/join`  
**Then** the backend broadcasts presence update to `/topic/presence/{roomId}`  
**And** all clients update their user list to show the new user as online  
**And** the user count increments

#### Scenario: User leaves chat room

**Given** a user is in a chat room  
**When** the user disconnects or navigates away  
**Then** the WebSocket session close event triggers  
**And** the backend broadcasts leave notification to `/topic/presence/{roomId}`  
**And** all clients remove the user from online list  
**And** the user count decrements

#### Scenario: Display online user count

**Given** 5 users are connected to room "general"  
**When** viewing the chat interface  
**Then** the header displays "5 users online"  
**And** the count updates in real-time as users join/leave

### Requirement: Retrieve chat history from database

The system SHALL load recent chat history when joining a room using reactive database queries.

**Priority:** Medium  
**Status:** Planned

#### Scenario: Load last 50 messages on room join

**Given** a chat room has 200 historical messages  
**When** a user joins the room  
**Then** the backend queries the last 50 messages using R2DBC  
**And** messages are returned as `Flux<ChatMessage>` ordered by timestamp descending  
**And** the frontend displays messages in chronological order (oldest first)

#### Scenario: Empty chat room history

**Given** a newly created chat room with no messages  
**When** a user joins  
**Then** the history query returns empty `Flux`  
**And** the frontend displays "No messages yet. Start the conversation!"

### Requirement: Handle message delivery failures gracefully

The system SHALL manage WebSocket errors and provide user feedback.

**Priority:** Medium  
**Status:** Planned

#### Scenario: Message send fails due to disconnect

**Given** a user is composing a message  
**When** the WebSocket disconnects before sending  
**Then** the send operation fails  
**And** the UI shows "Message not sent. Reconnecting..." notification  
**And** the message remains in input field for retry  
**And** after reconnection, the user can resend manually

#### Scenario: Database save fails

**Given** a message is received by backend  
**When** the R2DBC save operation fails (database down)  
**Then** the error is logged with message details  
**And** the message is NOT broadcast to clients  
**And** the sender receives an error notification via WebSocket

## Technical Notes

- **Backend:** `@MessageMapping` for incoming messages, `SimpMessagingTemplate` for broadcasting
- **WebSocket config:** Enables STOMP over SockJS with endpoint `/ws`
- **Database:** R2DBC `ChatMessageRepository extends ReactiveCrudRepository`
- **Frontend:** `@stomp/stompjs` library with subscription callbacks
- **Message format:** `{ id, roomId, userName, messageText, createdAt }`
- **Presence tracking:** In-memory map of session IDs to usernames (simple approach)
