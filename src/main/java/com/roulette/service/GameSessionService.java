package com.roulette.service;

import com.roulette.dto.GameSessionDTO;
import com.roulette.dto.SpinResultDTO;
import com.roulette.model.bet.Bet;
import com.roulette.model.entity.GameSession;
import com.roulette.model.entity.Player;
import com.roulette.model.perk.PerkType;

import java.util.List;
import java.util.Optional;

/**
 * Service managing game sessions, rounds, persistence, and perks (RQ02, RQ03, RQ05, RQ06, RQ10).
 */
public interface GameSessionService {
    GameSession createSession(String username);
    Optional<GameSession> getActiveSession(String username);
    void saveSession(GameSession session);
    SpinResultDTO processSpin(GameSession session, List<Bet> bets);
    void grantPerkReward(GameSession session, PerkType perk);
    void togglePerk(GameSession session, PerkType perk, boolean active);
    String revealNextNumber(GameSession session);
    GameSessionDTO toDTO(GameSession session, Player player);
}
