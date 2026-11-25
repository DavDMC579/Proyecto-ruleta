package com.roulette.dao.impl;

import com.roulette.config.DatabaseConfig;
import com.roulette.dao.PlayerDao;
import com.roulette.model.entity.Player;

import java.sql.*;
import java.util.Optional;

/**
 * PostgreSQL JDBC implementation of PlayerDao (RQ08, RQ11).
 */
public class PostgreSqlPlayerDao implements PlayerDao {

    @Override
    public Optional<Player> findById(Long id) {
        String sql = "SELECT id, username, created_at FROM players WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Player p = new Player();
                    p.setId(rs.getLong("id"));
                    p.setUsername(rs.getString("username"));
                    p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return Optional.of(p);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error in findById: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Player> findByUsername(String username) {
        String sql = "SELECT id, username, created_at FROM players WHERE username = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Player p = new Player();
                    p.setId(rs.getLong("id"));
                    p.setUsername(rs.getString("username"));
                    p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return Optional.of(p);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error in findByUsername: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Player save(Player player) {
        String sql = "INSERT INTO players (username) VALUES (?) RETURNING id, created_at";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, player.getUsername());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    player.setId(rs.getLong("id"));
                    player.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error in save player: " + e.getMessage());
        }
        return player;
    }
}
