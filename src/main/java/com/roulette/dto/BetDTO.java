package com.roulette.dto;

/**
 * Data Transfer Object representing a player's placed bet.
 */
public class BetDTO {
    private String betType;
    private String target;
    private long amount;

    public BetDTO() {}

    public BetDTO(String betType, String target, long amount) {
        this.betType = betType;
        this.target = target;
        this.amount = amount;
    }

    public String getBetType() {
        return betType;
    }

    public void setBetType(String betType) {
        this.betType = betType;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}
