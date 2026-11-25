package com.roulette;

import com.roulette.model.roulette.PocketColor;
import com.roulette.model.roulette.RoulettePocket;
import com.roulette.model.roulette.RouletteWheel;
import com.roulette.service.RouletteService;
import com.roulette.service.impl.RouletteServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests validating American Roulette wheel pockets and Clairvoyance predetermination (RQ12).
 */
public class RouletteWheelTest {

    @Test
    @DisplayName("Wheel should contain exactly 38 pockets (0, 00, 18 red, 18 black)")
    void testWheelPocketsCountAndColors() {
        RouletteWheel wheel = new RouletteWheel();
        List<RoulettePocket> pockets = wheel.getPockets();

        assertEquals(38, pockets.size());

        long redCount = pockets.stream().filter(p -> p.getColor() == PocketColor.RED).count();
        long blackCount = pockets.stream().filter(p -> p.getColor() == PocketColor.BLACK).count();
        long greenCount = pockets.stream().filter(p -> p.getColor() == PocketColor.GREEN).count();

        assertEquals(18, redCount);
        assertEquals(18, blackCount);
        assertEquals(2, greenCount); // '0' and '00'

        assertNotNull(wheel.getPocket("0"));
        assertNotNull(wheel.getPocket("00"));
        assertEquals(PocketColor.GREEN, wheel.getPocket("0").getColor());
        assertEquals(PocketColor.GREEN, wheel.getPocket("00").getColor());
    }

    @Test
    @DisplayName("RouletteService peekNextPocket should match subsequent spin (Clairvoyance functionality)")
    void testClairvoyancePredetermination() {
        RouletteService service = new RouletteServiceImpl();
        RoulettePocket peeked = service.peekNextPocket();
        assertNotNull(peeked);

        RoulettePocket spun = service.spin();
        assertEquals(peeked, spun, "The spun pocket must match the pre-revealed peeked pocket");
    }
}
