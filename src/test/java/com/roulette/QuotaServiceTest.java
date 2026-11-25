package com.roulette;

import com.roulette.service.QuotaService;
import com.roulette.service.impl.QuotaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests validating progressive quota calculation and dynamic scaling (RQ04, RQ12).
 */
public class QuotaServiceTest {
    private QuotaService quotaService;

    @BeforeEach
    void setUp() {
        quotaService = new QuotaServiceImpl();
    }

    @Test
    @DisplayName("When exceeding quota by a wide margin (e.g. 1000 target reached with 2000), next quota scales to 6000 (RQ04)")
    void testWideMarginQuotaScaling() {
        long previousQuota = 1000;
        long achievedBalance = 2000;

        // Exactly tests user's specification:
        // "ejemplo se inicia con 100 la cuota serian 1000 y digamos si el jugador supera los 1000 llega a 2000 la proxima cuota son 6000"
        long nextQuota = quotaService.calculateNextQuota(previousQuota, achievedBalance);
        assertEquals(6000, nextQuota);
    }

    @Test
    @DisplayName("Quota reached condition should return true when balance meets or exceeds target")
    void testIsQuotaReached() {
        assertTrue(quotaService.isQuotaReached(1000, 1000));
        assertTrue(quotaService.isQuotaReached(2500, 1000));
        assertFalse(quotaService.isQuotaReached(999, 1000));
    }
}
