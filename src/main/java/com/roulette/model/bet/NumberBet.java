package com.roulette.model.bet;

import com.roulette.model.roulette.RoulettePocket;
import java.util.Objects;

/**
 * Straight-up bet on a specific number (0, 00, 1-36).
 * Standard payout is 35 to 1 (returns 36x the bet).
 */
public class NumberBet implements Bet {
    private final String targetNumber;
    private final long amount;

    public NumberBet(String targetNumber, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Bet amount must be positive");
        }
        this.targetNumber = Objects.requireNonNull(targetNumber, "Target number cannot be null");
        this.amount = amount;
    }

    @Override
    public BetType getBetType() {
        return BetType.STRAIGHT_UP;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public boolean isWinning(RoulettePocket pocket) {
        return targetNumber.equals(pocket.getValue());
    }

    @Override
    public long calculatePayout(RoulettePocket pocket) {
        return isWinning(pocket) ? amount * 36 : 0;
    }

    @Override
    public String getTargetDescription() {
        return "Number " + targetNumber;
    }

    @Override
    public int getPayoutMultiplier() {
        return 35;
    }

    public String getTargetNumber() {
        return targetNumber;
    }
}
