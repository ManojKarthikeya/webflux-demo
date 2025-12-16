package com.demo.reactive.service;

import com.demo.reactive.model.ChatMessage;
import com.demo.reactive.repository.ChatMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ChatMessageService {
    
    private static final Logger log = LoggerFactory.getLogger(ChatMessageService.class);

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }
    
    /**
     * Save a chat message to the database
     * @param message the message to save
     * @return Mono of the saved message with generated ID
     */
    public Mono<ChatMessage> saveMessage(ChatMessage message) {
        log.debug("Saving chat message for room: {} from user: {}", 
                  message.getRoomId(), message.getUserName());
        return chatMessageRepository.save(message)
                .doOnSuccess(saved -> log.info("Message saved with ID: {}", saved.getId()))
                .doOnError(error -> log.error("Error saving message: {}", error.getMessage()));
    }
    
    /**
     * Get recent messages for a room
     * @param roomId the room ID
     * @param limit maximum number of messages to retrieve
     * @return Flux of recent chat messages
     */
    public Flux<ChatMessage> getRecentMessages(String roomId, int limit) {
        log.debug("Fetching {} recent messages for room: {}", limit, roomId);
        return chatMessageRepository.findRecentMessagesByRoomId(roomId, limit)
                .doOnComplete(() -> log.debug("Completed fetching messages for room: {}", roomId));
    }
    
    /**
     * Get all messages for a room ordered by creation time (descending)
     * @param roomId the room ID
     * @return Flux of all chat messages for the room
     */
    public Flux<ChatMessage> getAllMessagesByRoom(String roomId) {
        log.debug("Fetching all messages for room: {}", roomId);
        return chatMessageRepository.findByRoomIdOrderByCreatedAtDesc(roomId);
    }
}
