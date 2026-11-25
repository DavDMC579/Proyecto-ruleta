package com.roulette.dao.impl;

import com.roulette.config.DatabaseConfig;
import com.roulette.dao.GameSessionDao;
import com.roulette.model.entity.GameSession;
import com.roulette.model.perk.Perk;
import com.roulette.model.perk.PerkType;

import java.sql.*;
import java.util.Optional;

/**
 * PostgreSQL JDBC implementation of GameSessionDao (RQ08, RQ11).
 */
public class PostgreSqlGameSessionDao implements GameSessionDao {

    @Override
    public Optional<GameSession> findById(Long id) {
        String sql = "SELECT id, player_id, current_balance, current_quota, current_round, spins_left, status, created_at, updated_at " +
                     "FROM game_sessions WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSession(rs, conn));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error in findById session: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<GameSession> findActiveSessionByPlayerId(Long playerId) {
        String sql = "SELECT id, player_id, current_balance, current_quota, current_round, spins_left, status, created_at, updated_at " +
                     "FROM game_sessions WHERE player_id = ? AND status = 'ACTIVE' ORDER BY updated_at DESC LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSession(rs, conn));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error in findActiveSessionByPlayerId: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public GameSession save(GameSession session) {
        String sql = "INSERT INTO game_sessions (player_id, current_balance, current_quota, current_round, spins_left, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?) RETURNING id, created_at, updated_at";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, session.getPlayerId());
            ps.setLong(2, session.getCurrentBalance());
            ps.setLong(3, session.getCurrentQuota());
            ps.setInt(4, session.getCurrentRound());
            ps.setInt(5, session.getSpinsLeft());
            ps.setString(6, session.getStatus());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    session.setId(rs.getLong("id"));
                    session.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    session.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                }
            }
            savePerks(conn, session);
        } catch (SQLException e) {
            System.err.println("Database error in save session: " + e.getMessage());
        }
        return session;
    }

    @Override
    public void update(GameSession session) {
        String sql = "UPDATE game_sessions SET current_balance = ?, current_quota = ?, current_round = ?, " +
                     "spins_left = ?, status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, session.getCurrentBalance());
            ps.setLong(2, session.getCurrentQuota());
            ps.setInt(3, session.getCurrentRound());
            ps.setInt(4, session.getSpinsLeft());
            ps.setString(5, session.getStatus());
            ps.setLong(6, session.getId());

            ps.executeUpdate();
            savePerks(conn, session);
        } catch (SQLException e) {
            System.err.println("Database error in update session: " + e.getMessage());
        }
    }

    private void savePerks(Connection conn, GameSession session) throws SQLException {
        if (session.getId() == null) return;
        String deleteSql = "DELETE FROM game_perks WHERE session_id = ?";
        try (PreparedStatement psDel = conn.prepareStatement(deleteSql)) {
            psDel.setLong(1, session.getId());
            psDel.executeUpdate();
        }

        String insertSql = "INSERT INTO game_perks (session_id, perk_type, quantity, is_active) VALUES (?, ?, ?, ?)";
        try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
            for (Perk perk : session.getPerks().values()) {
                psIns.setLong(1, session.getId());
                psIns.setString(2, perk.getType().name());
                psIns.setInt(3, perk.getQuantity());
                psIns.setBoolean(4, perk.isActive());
                psIns.addBatch();
            }
            psIns.executeBatch();
        }
    }

    private GameSession mapResultSetToSession(ResultSet rs, Connection conn) throws SQLException {
        GameSession s = new GameSession();
        s.setId(rs.getLong("id"));
        s.setPlayerId(rs.getLong("player_id"));
        s.setCurrentBalance(rs.getLong("current_balance"));
        s.setCurrentQuota(rs.getLong("current_quota"));
        s.setCurrentRound(rs.getInt("current_round"));
        s.setSpinsLeft(rs.getInt("spins_left"));
        s.setStatus(rs.getString("status"));
        s.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        s.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        // Load perks
        String perkSql = "SELECT perk_type, quantity, is_active FROM game_perks WHERE session_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(perkSql)) {
            ps.setLong(1, s.getId());
            try (ResultSet prs = ps.executeQuery()) {
                while (prs.next()) {
                    PerkType type = PerkType.valueOf(prs.getString("perk_type"));
                    Perk p = s.getPerk(type);
                    if (p != null) {
                        p.setQuantity(prs.getInt("quantity"));
                        p.setActive(prs.getBoolean("is_active"));
                    }
                }
            }
        }
        return s;
    }
}
