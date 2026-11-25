package com.roulette.model.bet;

/**
 * Enumeration of supported roulette bet categories.
 */
public enum BetType {
    STRAIGHT_UP, // Specific number (e.g., 17, 0, 00) - pays 35:1
    COLOR,       // Red or Black - pays 1:1
    PARITY,      // Even or Odd - pays 1:1
    DOZEN,       // 1st 12, 2nd 12, 3rd 12 - pays 2:1
    RANGE        // 1-18 or 19-36 - pays 1:1
}
