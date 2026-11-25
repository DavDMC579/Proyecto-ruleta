package com.roulette.factory;

import com.roulette.model.bet.*;
import com.roulette.model.roulette.PocketColor;

/**
 * Factory class for creating Bet instances following the Factory Pattern (RQ11).
 */
public class BetFactory {

    public Bet createNumberBet(String number, long amount) {
        return new NumberBet(number, amount);
    }

    public Bet createColorBet(PocketColor color, long amount) {
        return new ColorBet(color, amount);
    }

    public Bet createParityBet(ParityBet.Parity parity, long amount) {
        return new ParityBet(parity, amount);
    }

    public Bet createDozenBet(DozenBet.Dozen dozen, long amount) {
        return new DozenBet(dozen, amount);
    }

    /**
     * General factory method that creates a bet based on type name and target string.
     */
    public Bet createBet(String typeName, String target, long amount) {
        if (typeName == null || target == null) {
            throw new IllegalArgumentException("Type name and target cannot be null");
        }

        switch (typeName.toUpperCase()) {
            case "STRAIGHT_UP":
            case "NUMBER":
                return createNumberBet(target, amount);

            case "COLOR":
                PocketColor color = PocketColor.valueOf(target.toUpperCase());
                return createColorBet(color, amount);

            case "PARITY":
                ParityBet.Parity parity = ParityBet.Parity.valueOf(target.toUpperCase());
                return createParityBet(parity, amount);

            case "DOZEN":
                DozenBet.Dozen dozen = DozenBet.Dozen.valueOf(target.toUpperCase());
                return createDozenBet(dozen, amount);

            default:
                throw new IllegalArgumentException("Unsupported bet type: " + typeName);
        }
    }
}
