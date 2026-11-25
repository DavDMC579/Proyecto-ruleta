package com.roulette.dao.impl;

import com.roulette.dao.GameSessionDao;
import com.roulette.model.entity.GameSession;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-Memory fallback implementation of GameSessionDao.
 */
public class InMemoryGameSessionDao implements GameSessionDao {
    private final Map<Long, GameSession> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    @Override
    public Optional<GameSession> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<GameSession> findActiveSessionByPlayerId(Long playerId) {
        return storage.values().stream()
                .filter(s -> s.getPlayerId().equals(playerId) && "ACTIVE".equals(s.getStatus()))
                .findFirst();
    }

    @Override
    public GameSession save(GameSession session) {
        if (session.getId() == null) {
            session.setId(idGen.getAndIncrement());
            session.setCreatedAt(LocalDateTime.now());
        }
        session.setUpdatedAt(LocalDateTime.now());
        storage.put(session.getId(), session);
        return session;
    }

    @Override
    public void update(GameSession session) {
        session.setUpdatedAt(LocalDateTime.now());
        storage.put(session.getId(), session);
    }
}
