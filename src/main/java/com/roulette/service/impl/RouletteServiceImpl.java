package com.roulette.service.impl;

import com.roulette.model.roulette.RoulettePocket;
import com.roulette.model.roulette.RouletteWheel;
import com.roulette.service.RouletteService;

import java.util.List;

/**
 * Implementation of RouletteService with support for wheel spinning and Clairvoyance predetermination.
 */
public class RouletteServiceImpl implements RouletteService {
    private final RouletteWheel wheel;
    private RoulettePocket predeterminedPocket;

    public RouletteServiceImpl() {
        this(new RouletteWheel());
    }

    public RouletteServiceImpl(RouletteWheel wheel) {
        this.wheel = wheel;
    }

    @Override
    public RoulettePocket spin() {
        if (predeterminedPocket != null) {
            RoulettePocket chosen = predeterminedPocket;
            predeterminedPocket = null;
            return chosen;
        }
        return wheel.spin();
    }

    @Override
    public RoulettePocket peekNextPocket() {
        if (predeterminedPocket == null) {
            predeterminedPocket = wheel.spin();
        }
        return predeterminedPocket;
    }

    @Override
    public List<RoulettePocket> getAllPockets() {
        return wheel.getPockets();
    }

    @Override
    public RoulettePocket getPocket(String value) {
        return wheel.getPocket(value);
    }
}
