package com.demo.reactive.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class PresenceService {
    
    private static final Logger log = LoggerFactory.getLogger(PresenceService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public PresenceService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    // Map of session ID to user information
    private final Map<String, UserPresence> sessions = new ConcurrentHashMap<>();
    
    // Map of room ID to set of session IDs
    private final Map<String, Set<String>> roomSessions = new ConcurrentHashMap<>();
    
    /**
     * Handle user joining a room
     */
    public void userJoined(String sessionId, String roomId, String userName) {
        log.info("User {} joined room {} with session {}", userName, roomId, sessionId);
        
        UserPresence presence = new UserPresence(sessionId, userName, roomId);
        sessions.put(sessionId, presence);
        
        roomSessions.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        
        broadcastPresenceUpdate(roomId);
    }
    
    /**
     * Handle user leaving a room
     */
    public void userLeft(String sessionId) {
        UserPresence presence = sessions.remove(sessionId);
        if (presence != null) {
            log.info("User {} left room {} with session {}", 
                     presence.userName, presence.roomId, sessionId);
            
            String roomId = presence.roomId;
            Set<String> roomSessionSet = roomSessions.get(roomId);
            if (roomSessionSet != null) {
                roomSessionSet.remove(sessionId);
                if (roomSessionSet.isEmpty()) {
                    roomSessions.remove(roomId);
                }
            }
            
            broadcastPresenceUpdate(roomId);
        }
    }
    
    /**
     * Get active users in a room
     */
    public Set<String> getActiveUsers(String roomId) {
        Set<String> sessionIds = roomSessions.get(roomId);
        if (sessionIds == null) {
            return Set.of();
        }
        
        return sessionIds.stream()
                .map(sessions::get)
                .filter(presence -> presence != null)
                .map(presence -> presence.userName)
                .collect(Collectors.toSet());
    }
    
    /**
     * Get user count for a room
     */
    public int getUserCount(String roomId) {
        Set<String> sessionIds = roomSessions.get(roomId);
        return sessionIds != null ? sessionIds.size() : 0;
    }
    
    /**
     * Broadcast presence update to all users in a room
     */
    private void broadcastPresenceUpdate(String roomId) {
        PresenceUpdate update = new PresenceUpdate(
                getActiveUsers(roomId),
                getUserCount(roomId)
        );
        
        log.debug("Broadcasting presence update for room {}: {} users", 
                  roomId, update.userCount);
        
        messagingTemplate.convertAndSend("/topic/presence/" + roomId, update);
    }
    
    /**
     * Handle WebSocket connection event
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        log.info("WebSocket connection established: {}", sessionId);
    }
    
    /**
     * Handle WebSocket disconnection event
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        log.info("WebSocket disconnection: {}", sessionId);
        
        userLeft(sessionId);
    }
    
    // Inner classes for data structures
    private record UserPresence(String sessionId, String userName, String roomId) {}
    
    public record PresenceUpdate(Set<String> activeUsers, int userCount) {}
}
