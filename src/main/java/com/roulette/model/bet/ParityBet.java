package com.roulette.model.bet;

import com.roulette.model.roulette.RoulettePocket;

/**
 * Bet placed on Even or Odd parity.
 * Standard payout is 1 to 1 (returns 2x the bet).
 * Note: 0 and 00 do not count as even or odd.
 */
public class ParityBet implements Bet {
    public enum Parity { EVEN, ODD }

    private final Parity targetParity;
    private final long amount;

    public ParityBet(Parity targetParity, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Bet amount must be positive");
        }
        if (targetParity == null) {
            throw new IllegalArgumentException("Target parity cannot be null");
        }
        this.targetParity = targetParity;
        this.amount = amount;
    }

    @Override
    public BetType getBetType() {
        return BetType.PARITY;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public boolean isWinning(RoulettePocket pocket) {
        if (pocket.isZero()) return false;
        if (targetParity == Parity.EVEN) {
            return pocket.isEven();
        } else {
            return pocket.isOdd();
        }
    }

    @Override
    public long calculatePayout(RoulettePocket pocket) {
        return isWinning(pocket) ? amount * 2 : 0;
    }

    @Override
    public String getTargetDescription() {
        return "Parity " + targetParity;
    }

    @Override
    public int getPayoutMultiplier() {
        return 1;
    }

    public Parity getTargetParity() {
        return targetParity;
    }
}
