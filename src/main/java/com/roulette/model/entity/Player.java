package com.roulette.model.entity;

import java.time.LocalDateTime;

/**
 * Entity representing a player in the system.
 */
public class Player {
    private Long id;
    private String username;
    private LocalDateTime createdAt;

    public Player() {
        this.createdAt = LocalDateTime.now();
    }

    public Player(Long id, String username) {
        this.id = id;
        this.username = username;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
