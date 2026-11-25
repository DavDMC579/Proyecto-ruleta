package com.roulette;

import com.roulette.factory.BetFactory;
import com.roulette.model.bet.*;
import com.roulette.model.roulette.PocketColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests validating Factory pattern for Bets (RQ11, RQ12).
 */
public class BetFactoryTest {
    private BetFactory betFactory;

    @BeforeEach
    void setUp() {
        betFactory = new BetFactory();
    }

    @Test
    @DisplayName("Factory should correctly instantiate all bet subtypes")
    void testBetFactoryTypes() {
        Bet numBet = betFactory.createNumberBet("7", 100);
        assertTrue(numBet instanceof NumberBet);
        assertEquals(BetType.STRAIGHT_UP, numBet.getBetType());
        assertEquals(100, numBet.getAmount());

        Bet colorBet = betFactory.createColorBet(PocketColor.RED, 50);
        assertTrue(colorBet instanceof ColorBet);
        assertEquals(BetType.COLOR, colorBet.getBetType());

        Bet parityBet = betFactory.createParityBet(ParityBet.Parity.EVEN, 25);
        assertTrue(parityBet instanceof ParityBet);
        assertEquals(BetType.PARITY, parityBet.getBetType());

        Bet dozenBet = betFactory.createDozenBet(DozenBet.Dozen.SECOND, 75);
        assertTrue(dozenBet instanceof DozenBet);
        assertEquals(BetType.DOZEN, dozenBet.getBetType());
    }

    @Test
    @DisplayName("Factory should dynamically parse and instantiate bet from string parameters")
    void testDynamicBetCreation() {
        Bet bet = betFactory.createBet("COLOR", "BLACK", 40);
        assertNotNull(bet);
        assertTrue(bet instanceof ColorBet);
        assertEquals(PocketColor.BLACK, ((ColorBet) bet).getTargetColor());
    }
}
