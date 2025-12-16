package com.demo.reactive.controller;

import com.demo.reactive.model.ChatMessage;
import com.demo.reactive.service.ChatMessageService;
import com.demo.reactive.service.PresenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Controller
public class ChatController {
    
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final PresenceService presenceService;

    public ChatController(ChatMessageService chatMessageService, SimpMessagingTemplate messagingTemplate, PresenceService presenceService) {
        this.chatMessageService = chatMessageService;
        this.messagingTemplate = messagingTemplate;
        this.presenceService = presenceService;
    }
    
    /**
     * Handle incoming chat messages via WebSocket
     * @param roomId the room to send message to
     * @param message the chat message
     */
    @MessageMapping("/chat/{roomId}")
    public void handleChatMessage(@DestinationVariable String roomId, @Payload ChatMessage message) {
        log.info("Received message for room: {} from user: {}", roomId, message.getUserName());
        
        // Set room ID and timestamp
        message.setRoomId(roomId);
        message.setCreatedAt(LocalDateTime.now());
        
        // Save to database and broadcast on success
        chatMessageService.saveMessage(message)
                .subscribe(
                        savedMessage -> {
                            log.info("Broadcasting message ID: {} to /topic/chat/{}", 
                                     savedMessage.getId(), roomId);
                            messagingTemplate.convertAndSend(
                                    "/topic/chat/" + roomId, 
                                    savedMessage
                            );
                        },
                        error -> log.error("Error saving/broadcasting message: {}", error.getMessage())
                );
    }
    
    /**
     * REST endpoint to get chat history for a room
     * @param roomId the room ID
     * @param limit optional limit on number of messages (default 50)
     * @return Flux of recent chat messages
     */
    @GetMapping("/api/chat/{roomId}/history")
    @ResponseBody
    public Flux<ChatMessage> getChatHistory(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "50") int limit) {
        log.info("Fetching chat history for room: {} with limit: {}", roomId, limit);
        return chatMessageService.getRecentMessages(roomId, limit)
                .sort((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt())); // Return chronological order
    }
    
    /**
     * REST endpoint to get all messages for a room
     * @param roomId the room ID
     * @return Flux of all chat messages
     */
    @GetMapping("/api/chat/{roomId}/messages")
    @ResponseBody
    public Flux<ChatMessage> getAllMessages(@PathVariable String roomId) {
        log.info("Fetching all messages for room: {}", roomId);
        return chatMessageService.getAllMessagesByRoom(roomId);
    }
    
    /**
     * Handle user joining a room
     * @param roomId the room ID
     * @param payload containing userName
     * @param headerAccessor for accessing session information
     */
    @MessageMapping("/chat/join/{roomId}")
    public void handleUserJoin(
            @DestinationVariable String roomId,
            @Payload Map<String, String> payload,
            SimpMessageHeaderAccessor headerAccessor) {
        String userName = payload.get("userName");
        String sessionId = headerAccessor.getSessionId();
        
        log.info("User {} joining room {} with session {}", userName, roomId, sessionId);
        presenceService.userJoined(sessionId, roomId, userName);
    }
    
    /**
     * REST endpoint to get active users in a room
     * @param roomId the room ID
     * @return Mono containing active users information
     */
    @GetMapping("/api/chat/{roomId}/users")
    @ResponseBody
    public Mono<Map<String, Object>> getActiveUsers(@PathVariable String roomId) {
        Set<String> activeUsers = presenceService.getActiveUsers(roomId);
        int userCount = presenceService.getUserCount(roomId);
        
        return Mono.just(Map.of(
                "activeUsers", activeUsers,
                "userCount", userCount
        ));
    }
}
