package com.roulette.dto;

import com.roulette.model.perk.PerkType;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Data Transfer Object for Game Session state (RQ06, RQ11).
 */
public class GameSessionDTO {
    private Long id;
    private Long playerId;
    private String playerName;
    private long currentBalance;
    private long currentQuota;
    private int currentRound;
    private int spinsLeft;
    private String status;
    private Map<PerkType, Integer> perkInventory;
    private LocalDateTime updatedAt;

    public GameSessionDTO() {}

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

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
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

    public Map<PerkType, Integer> getPerkInventory() {
        return perkInventory;
    }

    public void setPerkInventory(Map<PerkType, Integer> perkInventory) {
        this.perkInventory = perkInventory;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
