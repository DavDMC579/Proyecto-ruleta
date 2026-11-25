package com.roulette.service;

import com.roulette.model.bet.Bet;
import com.roulette.model.roulette.RoulettePocket;

import java.util.List;

/**
 * Service interface for processing and evaluating bets (RQ01, RQ03, RQ05, RQ12).
 */
public interface BettingService {
    long calculateTotalBet(List<Bet> bets);
    boolean validateMinimumBet(List<Bet> bets, long minimumRequired);
    long evaluatePayouts(List<Bet> bets, RoulettePocket winningPocket);
    long applyLossMitigation(long totalBet, long payout);
}
