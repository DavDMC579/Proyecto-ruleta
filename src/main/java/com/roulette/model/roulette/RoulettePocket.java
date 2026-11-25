package com.roulette.model.roulette;

import java.util.Objects;

/**
 * Represents a specific pocket on the roulette wheel.
 */
public class RoulettePocket {
    private final String value;
    private final PocketColor color;
    private final Integer numericValue;

    public RoulettePocket(String value, PocketColor color) {
        this.value = value;
        this.color = color;
        if ("0".equals(value) || "00".equals(value)) {
            this.numericValue = null;
        } else {
            this.numericValue = Integer.parseInt(value);
        }
    }

    public String getValue() {
        return value;
    }

    public PocketColor getColor() {
        return color;
    }

    public Integer getNumericValue() {
        return numericValue;
    }

    public boolean isZero() {
        return "0".equals(value) || "00".equals(value);
    }

    public boolean isEven() {
        return numericValue != null && numericValue % 2 == 0;
    }

    public boolean isOdd() {
        return numericValue != null && numericValue % 2 != 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoulettePocket that = (RoulettePocket) o;
        return Objects.equals(value, that.value) && color == that.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, color);
    }

    @Override
    public String toString() {
        return value + " (" + color + ")";
    }
}
