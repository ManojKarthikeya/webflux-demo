package com.demo.reactive.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("chat_messages")
public class ChatMessage {
    
    @Id
    private Long id;
    
    private String roomId;
    
    private String userName;
    
    private String messageText;
    
    private LocalDateTime createdAt;

    public ChatMessage() {
    }

    public ChatMessage(Long id, String roomId, String userName, String messageText, LocalDateTime createdAt) {
        this.id = id;
        this.roomId = roomId;
        this.userName = userName;
        this.messageText = messageText;
        this.createdAt = createdAt;
    }
    
    public ChatMessage(String roomId, String userName, String messageText) {
        this.roomId = roomId;
        this.userName = userName;
        this.messageText = messageText;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "id=" + id +
                ", roomId='" + roomId + '\'' +
                ", userName='" + userName + '\'' +
                ", messageText='" + messageText + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
