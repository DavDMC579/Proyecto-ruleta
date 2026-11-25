package com.roulette.model.perk;

/**
 * Represents a player's acquired perk instance.
 */
public class Perk {
    private final PerkType type;
    private int quantity;
    private boolean active;

    public Perk(PerkType type, int quantity) {
        this.type = type;
        this.quantity = Math.max(0, quantity);
        this.active = false;
    }

    public PerkType getType() {
        return type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(0, quantity);
    }

    public void incrementQuantity() {
        this.quantity++;
    }

    public boolean decrementQuantity() {
        if (quantity > 0) {
            quantity--;
            return true;
        }
        return false;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
