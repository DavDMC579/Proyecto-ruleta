package com.roulette.model.perk;

/**
 * Strategic perks available upon clearing a round quota (RQ05).
 */
public enum PerkType {
    MONEY_BOOST("Money Boost", "Adds +50% of base initial balance to current funds"),
    CLAIRVOYANCE("Clairvoyance", "Reveals the exact winning number before the wheel spins"),
    LOSS_MITIGATION("Loss Mitigation", "Refunds 50% of losses if the bet does not win");

    private final String displayName;
    private final String description;

    PerkType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
