package com.roulette;

import com.roulette.factory.BetFactory;
import com.roulette.model.bet.*;
import com.roulette.model.roulette.PocketColor;
import com.roulette.model.roulette.RoulettePocket;
import com.roulette.service.BettingService;
import com.roulette.service.impl.BettingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests validating mathematical betting payouts and loss mitigation (RQ12).
 */
public class BetMathTest {
    private BettingService bettingService;
    private BetFactory betFactory;

    @BeforeEach
    void setUp() {
        bettingService = new BettingServiceImpl();
        betFactory = new BetFactory();
    }

    @Test
    @DisplayName("Straight-up bet should pay 35 to 1 (36x return) when matching winning number")
    void testStraightUpBetPayout() {
        Bet bet = betFactory.createNumberBet("17", 100);
        RoulettePocket winningPocket = new RoulettePocket("17", PocketColor.BLACK);
        RoulettePocket losingPocket = new RoulettePocket("18", PocketColor.RED);

        assertTrue(bet.isWinning(winningPocket));
        assertEquals(3600, bet.calculatePayout(winningPocket));

        assertFalse(bet.isWinning(losingPocket));
        assertEquals(0, bet.calculatePayout(losingPocket));
    }

    @Test
    @DisplayName("Color bet on RED should pay 1 to 1 (2x return)")
    void testColorBetPayout() {
        Bet redBet = betFactory.createColorBet(PocketColor.RED, 50);
        RoulettePocket redPocket = new RoulettePocket("7", PocketColor.RED);
        RoulettePocket blackPocket = new RoulettePocket("8", PocketColor.BLACK);
        RoulettePocket zeroPocket = new RoulettePocket("0", PocketColor.GREEN);

        assertTrue(redBet.isWinning(redPocket));
        assertEquals(100, redBet.calculatePayout(redPocket));

        assertFalse(redBet.isWinning(blackPocket));
        assertEquals(0, redBet.calculatePayout(blackPocket));

        assertFalse(redBet.isWinning(zeroPocket));
        assertEquals(0, redBet.calculatePayout(zeroPocket));
    }

    @Test
    @DisplayName("Parity bet on EVEN should pay 1 to 1 and not win on 0 or 00")
    void testParityBetPayout() {
        Bet evenBet = betFactory.createParityBet(ParityBet.Parity.EVEN, 50);
        RoulettePocket evenPocket = new RoulettePocket("14", PocketColor.RED);
        RoulettePocket oddPocket = new RoulettePocket("15", PocketColor.BLACK);
        RoulettePocket zeroPocket = new RoulettePocket("0", PocketColor.GREEN);
        RoulettePocket doubleZero = new RoulettePocket("00", PocketColor.GREEN);

        assertTrue(evenBet.isWinning(evenPocket));
        assertEquals(100, evenBet.calculatePayout(evenPocket));

        assertFalse(evenBet.isWinning(oddPocket));
        assertFalse(evenBet.isWinning(zeroPocket));
        assertFalse(evenBet.isWinning(doubleZero));
    }

    @Test
    @DisplayName("Dozen bet should pay 2 to 1 (3x return)")
    void testDozenBetPayout() {
        Bet dozen1 = betFactory.createDozenBet(DozenBet.Dozen.FIRST, 100);
        RoulettePocket inRange = new RoulettePocket("12", PocketColor.RED);
        RoulettePocket outOfRange = new RoulettePocket("13", PocketColor.BLACK);

        assertTrue(dozen1.isWinning(inRange));
        assertEquals(300, dozen1.calculatePayout(inRange));

        assertFalse(dozen1.isWinning(outOfRange));
        assertEquals(0, dozen1.calculatePayout(outOfRange));
    }

    @Test
    @DisplayName("Loss Mitigation should refund 50% of net loss when player loses")
    void testLossMitigationCalculation() {
        long totalBet = 200;
        long zeroPayout = 0;

        long mitigatedPayout = bettingService.applyLossMitigation(totalBet, zeroPayout);
        // Net loss is 200 -> refund 50% = 100
        assertEquals(100, mitigatedPayout);
    }
}
