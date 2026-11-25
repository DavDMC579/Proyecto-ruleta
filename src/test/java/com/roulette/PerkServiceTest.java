package com.roulette;

import com.roulette.dao.impl.InMemoryGameSessionDao;
import com.roulette.dao.impl.InMemoryPlayerDao;
import com.roulette.model.entity.GameSession;
import com.roulette.model.perk.PerkType;
import com.roulette.service.GameSessionService;
import com.roulette.service.impl.BettingServiceImpl;
import com.roulette.service.impl.GameSessionServiceImpl;
import com.roulette.service.impl.QuotaServiceImpl;
import com.roulette.service.impl.RouletteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests validating Strategic Perks: Money Boost, Clairvoyance, Loss Mitigation (RQ05, RQ12).
 */
public class PerkServiceTest {
    private GameSessionService gameSessionService;

    @BeforeEach
    void setUp() {
        gameSessionService = new GameSessionServiceImpl(
                new InMemoryPlayerDao(),
                new InMemoryGameSessionDao(),
                new RouletteServiceImpl(),
                new BettingServiceImpl(),
                new QuotaServiceImpl()
        );
    }

    @Test
    @DisplayName("Money Boost perk should grant +50% of base initial balance ($50) (RQ05)")
    void testMoneyBoostPerk() {
        GameSession session = gameSessionService.createSession("PlayerPerk");
        session.setCurrentBalance(1200);

        gameSessionService.grantPerkReward(session, PerkType.MONEY_BOOST);

        // 1200 + 50 = 1250
        assertEquals(1250, session.getCurrentBalance());
        assertEquals(2, session.getCurrentRound());
    }

    @Test
    @DisplayName("Granting Clairvoyance or Loss Mitigation should increase inventory quantity (RQ05)")
    void testInventoryPerks() {
        GameSession session = gameSessionService.createSession("PlayerPerk");

        assertEquals(0, session.getPerk(PerkType.CLAIRVOYANCE).getQuantity());
        assertEquals(0, session.getPerk(PerkType.LOSS_MITIGATION).getQuantity());

        gameSessionService.grantPerkReward(session, PerkType.CLAIRVOYANCE);
        assertEquals(1, session.getPerk(PerkType.CLAIRVOYANCE).getQuantity());

        gameSessionService.grantPerkReward(session, PerkType.LOSS_MITIGATION);
        assertEquals(1, session.getPerk(PerkType.LOSS_MITIGATION).getQuantity());
    }
}
