package com.demo.reactive.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("users")
public class User {
    
    @Id
    private Long id;
    
    private String username;
    
    private String displayName;
    
    private LocalDateTime createdAt;

    public User() {
    }

    public User(Long id, String username, String displayName, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.createdAt = createdAt;
    }
    
    public User(String username, String displayName) {
        this.username = username;
        this.displayName = displayName;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", displayName='" + displayName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
