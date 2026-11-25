package com.roulette.model.bet;

import com.roulette.model.roulette.RoulettePocket;

/**
 * Bet placed on 1st 12 (1-12), 2nd 12 (13-24), or 3rd 12 (25-36).
 * Standard payout is 2 to 1 (returns 3x the bet).
 */
public class DozenBet implements Bet {
    public enum Dozen { FIRST, SECOND, THIRD }

    private final Dozen dozen;
    private final long amount;

    public DozenBet(Dozen dozen, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Bet amount must be positive");
        }
        if (dozen == null) {
            throw new IllegalArgumentException("Dozen cannot be null");
        }
        this.dozen = dozen;
        this.amount = amount;
    }

    @Override
    public BetType getBetType() {
        return BetType.DOZEN;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public boolean isWinning(RoulettePocket pocket) {
        if (pocket.isZero() || pocket.getNumericValue() == null) return false;
        int num = pocket.getNumericValue();
        switch (dozen) {
            case FIRST:
                return num >= 1 && num <= 12;
            case SECOND:
                return num >= 13 && num <= 24;
            case THIRD:
                return num >= 25 && num <= 36;
            default:
                return false;
        }
    }

    @Override
    public long calculatePayout(RoulettePocket pocket) {
        return isWinning(pocket) ? amount * 3 : 0;
    }

    @Override
    public String getTargetDescription() {
        switch (dozen) {
            case FIRST: return "1st 12 (1-12)";
            case SECOND: return "2nd 12 (13-24)";
            case THIRD: return "3rd 12 (25-36)";
            default: return "Dozen";
        }
    }

    @Override
    public int getPayoutMultiplier() {
        return 2;
    }

    public Dozen getDozen() {
        return dozen;
    }
}
