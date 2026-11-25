package com.roulette.service.impl;

import com.roulette.service.QuotaService;

/**
 * Implementation of QuotaService (RQ02, RQ03, RQ04).
 */
public class QuotaServiceImpl implements QuotaService {

    @Override
    public long calculateNextQuota(long previousQuota, long currentBalance) {
        if (previousQuota <= 0) previousQuota = 1000;

        // RQ04: Dynamic quota scaling proportionally to difficulty if exceeded by a wide margin
        // Example: Base 100 -> Quota 1,000 -> Reached 2,000 -> Next Quota: 6,000
        double ratio = (double) currentBalance / previousQuota;

        if (ratio >= 1.5) {
            // Wide margin: scales 3x the current balance
            return Math.max(previousQuota * 2, Math.round((currentBalance * 3.0) / 100.0) * 100);
        } else {
            // Standard progressive scaling
            return Math.max(previousQuota * 2, Math.round((previousQuota * 2.5) / 100.0) * 100);
        }
    }

    @Override
    public boolean isQuotaReached(long currentBalance, long targetQuota) {
        return currentBalance >= targetQuota;
    }

    @Override
    public long getMinimumMandatoryBet(int round) {
        // RQ03: Mandatory minimum bet per round
        // Starts at 10 and slightly scales with round
        return Math.max(10, (long) round * 10);
    }
}
