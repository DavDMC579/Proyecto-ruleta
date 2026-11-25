package com.roulette.model.bet;

import com.roulette.model.roulette.RoulettePocket;

/**
 * Interface defining a roulette bet contract.
 */
public interface Bet {
    BetType getBetType();
    long getAmount();
    boolean isWinning(RoulettePocket pocket);
    long calculatePayout(RoulettePocket pocket);
    String getTargetDescription();
    int getPayoutMultiplier();
}
