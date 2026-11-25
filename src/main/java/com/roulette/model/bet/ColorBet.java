package com.roulette.model.bet;

import com.roulette.model.roulette.PocketColor;
import com.roulette.model.roulette.RoulettePocket;
import java.util.Objects;

/**
 * Bet placed on Red or Black.
 * Standard payout is 1 to 1 (returns 2x the bet).
 */
public class ColorBet implements Bet {
    private final PocketColor targetColor;
    private final long amount;

    public ColorBet(PocketColor targetColor, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Bet amount must be positive");
        }
        if (targetColor == PocketColor.GREEN) {
            throw new IllegalArgumentException("Color bet must be RED or BLACK");
        }
        this.targetColor = Objects.requireNonNull(targetColor, "Target color cannot be null");
        this.amount = amount;
    }

    @Override
    public BetType getBetType() {
        return BetType.COLOR;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public boolean isWinning(RoulettePocket pocket) {
        return targetColor == pocket.getColor();
    }

    @Override
    public long calculatePayout(RoulettePocket pocket) {
        return isWinning(pocket) ? amount * 2 : 0;
    }

    @Override
    public String getTargetDescription() {
        return "Color " + targetColor;
    }

    @Override
    public int getPayoutMultiplier() {
        return 1;
    }

    public PocketColor getTargetColor() {
        return targetColor;
    }
}
