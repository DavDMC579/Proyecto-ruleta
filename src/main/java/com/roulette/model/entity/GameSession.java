package com.roulette.model.entity;

import com.roulette.model.perk.Perk;
import com.roulette.model.perk.PerkType;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

/**
 * Entity representing an ongoing or completed game session (RQ06, RQ08).
 */
public class GameSession {
    private Long id;
    private Long playerId;
    private long currentBalance;
    private long currentQuota;
    private int currentRound;
    private int spinsLeft;
    private String status; // ACTIVE, WON, GAME_OVER
    private final Map<PerkType, Perk> perks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public GameSession() {
        this.currentBalance = 100;
        this.currentQuota = 1000;
        this.currentRound = 1;
        this.spinsLeft = 6;
        this.status = "ACTIVE";
        this.perks = new EnumMap<>(PerkType.class);
        for (PerkType type : PerkType.values()) {
            this.perks.put(type, new Perk(type, 0));
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public long getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(long currentBalance) {
        this.currentBalance = currentBalance;
    }

    public long getCurrentQuota() {
        return currentQuota;
    }

    public void setCurrentQuota(long currentQuota) {
        this.currentQuota = currentQuota;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public int getSpinsLeft() {
        return spinsLeft;
    }

    public void setSpinsLeft(int spinsLeft) {
        this.spinsLeft = spinsLeft;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<PerkType, Perk> getPerks() {
        return perks;
    }

    public Perk getPerk(PerkType type) {
        return perks.get(type);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
