package com.roulette;

import com.roulette.dao.impl.InMemoryGameSessionDao;
import com.roulette.dao.impl.InMemoryPlayerDao;
import com.roulette.factory.BetFactory;
import com.roulette.model.bet.Bet;
import com.roulette.model.entity.GameSession;
import com.roulette.model.roulette.PocketColor;
import com.roulette.service.GameSessionService;
import com.roulette.service.impl.BettingServiceImpl;
import com.roulette.service.impl.GameSessionServiceImpl;
import com.roulette.service.impl.QuotaServiceImpl;
import com.roulette.service.impl.RouletteServiceImpl;
import com.roulette.dto.SpinResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests validating balance management and minimum mandatory bets (RQ03, RQ12).
 */
public class BalanceServiceTest {
    private GameSessionService gameSessionService;
    private BetFactory betFactory;

    @BeforeEach
    void setUp() {
        betFactory = new BetFactory();
        gameSessionService = new GameSessionServiceImpl(
                new InMemoryPlayerDao(),
                new InMemoryGameSessionDao(),
                new RouletteServiceImpl(),
                new BettingServiceImpl(),
                new QuotaServiceImpl()
        );
    }

    @Test
    @DisplayName("New session should initialize with $100 balance and $1,000 target quota")
    void testInitialBalance() {
        GameSession session = gameSessionService.createSession("Tester");
        assertNotNull(session);
        assertEquals(100, session.getCurrentBalance());
        assertEquals(1000, session.getCurrentQuota());
        assertEquals(1, session.getCurrentRound());
        assertEquals(6, session.getSpinsLeft());
        assertEquals("ACTIVE", session.getStatus());
    }

    @Test
    @DisplayName("Should trigger GAME OVER when player funds are insufficient to cover minimum mandatory bet (RQ03)")
    void testMinimumMandatoryBetEnforcement() {
        GameSession session = gameSessionService.createSession("Tester");
        // Reduce balance below round 1 minimum bet ($10)
        session.setCurrentBalance(5);

        Bet underfundedBet = betFactory.createColorBet(PocketColor.RED, 5);
        SpinResultDTO result = gameSessionService.processSpin(session, Collections.singletonList(underfundedBet));

        assertTrue(result.isGameOver());
        assertEquals("GAME_OVER", session.getStatus());
        assertTrue(result.getMessage().contains("Fondos insuficientes"));
    }
}
