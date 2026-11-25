package com.roulette.service.impl;

import com.roulette.model.bet.Bet;
import com.roulette.model.roulette.RoulettePocket;
import com.roulette.service.BettingService;

import java.util.List;

/**
 * Implementation of BettingService.
 */
public class BettingServiceImpl implements BettingService {

    @Override
    public long calculateTotalBet(List<Bet> bets) {
        if (bets == null) return 0;
        return bets.stream().mapToLong(Bet::getAmount).sum();
    }

    @Override
    public boolean validateMinimumBet(List<Bet> bets, long minimumRequired) {
        return calculateTotalBet(bets) >= minimumRequired;
    }

    @Override
    public long evaluatePayouts(List<Bet> bets, RoulettePocket winningPocket) {
        if (bets == null || winningPocket == null) return 0;
        return bets.stream()
                .mapToLong(bet -> bet.calculatePayout(winningPocket))
                .sum();
    }

    @Override
    public long applyLossMitigation(long totalBet, long payout) {
        // Loss Mitigation (RQ05): If loss occurs, refund 50% of the lost amount
        long netLoss = totalBet - payout;
        if (netLoss > 0) {
            long refund = netLoss / 2;
            return payout + refund;
        }
        return payout;
    }
}
