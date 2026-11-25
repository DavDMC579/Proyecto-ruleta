package com.roulette.dao.impl;

import com.roulette.dao.PlayerDao;
import com.roulette.model.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-Memory fallback implementation of PlayerDao.
 */
public class InMemoryPlayerDao implements PlayerDao {
    private final Map<Long, Player> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    @Override
    public Optional<Player> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Player> findByUsername(String username) {
        return storage.values().stream()
                .filter(p -> p.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    @Override
    public Player save(Player player) {
        if (player.getId() == null) {
            player.setId(idGen.getAndIncrement());
        }
        storage.put(player.getId(), player);
        return player;
    }
}
