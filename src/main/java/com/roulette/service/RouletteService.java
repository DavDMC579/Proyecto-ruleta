package com.roulette.service;

import com.roulette.model.roulette.RoulettePocket;
import java.util.List;

/**
 * Service interface for Roulette wheel operations.
 */
public interface RouletteService {
    RoulettePocket spin();
    RoulettePocket peekNextPocket();
    List<RoulettePocket> getAllPockets();
    RoulettePocket getPocket(String value);
}
