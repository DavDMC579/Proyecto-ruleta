package com.roulette.service;

/**
 * Service interface for dynamic progressive quota calculations (RQ04).
 */
public interface QuotaService {
    long calculateNextQuota(long previousQuota, long currentBalance);
    boolean isQuotaReached(long currentBalance, long targetQuota);
    long getMinimumMandatoryBet(int round);
}
