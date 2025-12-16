package com.demo.reactive.repository;

import com.demo.reactive.model.ChatMessage;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ChatMessageRepository extends ReactiveCrudRepository<ChatMessage, Long> {
    
    @Query("SELECT * FROM chat_messages WHERE room_id = :roomId ORDER BY created_at DESC LIMIT :limit")
    Flux<ChatMessage> findRecentMessagesByRoomId(String roomId, int limit);
    
    Flux<ChatMessage> findByRoomIdOrderByCreatedAtDesc(String roomId);
}
