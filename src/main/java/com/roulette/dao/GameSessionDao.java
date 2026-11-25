package com.roulette.dao;

import com.roulette.model.entity.GameSession;
import java.util.Optional;

/**
 * Data Access Object interface for GameSession entities (RQ08, RQ11).
 */
public interface GameSessionDao {
    Optional<GameSession> findById(Long id);
    Optional<GameSession> findActiveSessionByPlayerId(Long playerId);
    GameSession save(GameSession session);
    void update(GameSession session);
}
