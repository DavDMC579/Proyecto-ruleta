package com.roulette.dao;

import com.roulette.model.entity.Player;
import java.util.Optional;

/**
 * Data Access Object interface for Player entities (RQ11).
 */
public interface PlayerDao {
    Optional<Player> findById(Long id);
    Optional<Player> findByUsername(String username);
    Player save(Player player);
}
