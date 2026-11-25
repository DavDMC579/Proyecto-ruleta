package com.roulette.dto;

import java.util.List;

/**
 * Data Transfer Object encapsulating the result of a wheel spin.
 */
public class SpinResultDTO {
    private String winningPocket;
    private String pocketColor;
    private long totalBetAmount;
    private long totalPayout;
    private long netProfit;
    private long newBalance;
    private boolean quotaReached;
    private boolean gameOver;
    private String message;
    private List<String> activePerksUsed;

    public SpinResultDTO() {}

    public String getWinningPocket() {
        return winningPocket;
    }

    public void setWinningPocket(String winningPocket) {
        this.winningPocket = winningPocket;
    }

    public String getPocketColor() {
        return pocketColor;
    }

    public void setPocketColor(String pocketColor) {
        this.pocketColor = pocketColor;
    }

    public long getTotalBetAmount() {
        return totalBetAmount;
    }

    public void setTotalBetAmount(long totalBetAmount) {
        this.totalBetAmount = totalBetAmount;
    }

    public long getTotalPayout() {
        return totalPayout;
    }

    public void setTotalPayout(long totalPayout) {
        this.totalPayout = totalPayout;
    }

    public long getNetProfit() {
        return netProfit;
    }

    public void setNetProfit(long netProfit) {
        this.netProfit = netProfit;
    }

    public long getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(long newBalance) {
        this.newBalance = newBalance;
    }

    public boolean isQuotaReached() {
        return quotaReached;
    }

    public void setQuotaReached(boolean quotaReached) {
        this.quotaReached = quotaReached;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getActivePerksUsed() {
        return activePerksUsed;
    }

    public void setActivePerksUsed(List<String> activePerksUsed) {
        this.activePerksUsed = activePerksUsed;
    }
}
