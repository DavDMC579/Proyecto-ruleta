package com.roulette.model.roulette;

import java.security.SecureRandom;
import java.util.*;

/**
 * Encapsulates the American Roulette Wheel containing 38 pockets (0, 00, 1-36).
 */
public class RouletteWheel {
    private final List<RoulettePocket> pockets;
    private final Map<String, RoulettePocket> pocketMap;
    private final Random random;

    private static final Set<Integer> RED_NUMBERS = new HashSet<>(Arrays.asList(
            1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36
    ));

    public RouletteWheel() {
        this(new SecureRandom());
    }

    public RouletteWheel(Random random) {
        this.random = random;
        this.pockets = new ArrayList<>();
        this.pocketMap = new HashMap<>();
        initializePockets();
    }

    private void initializePockets() {
        // American roulette pocket sequence
        String[] sequence = {
                "0", "28", "9", "26", "30", "11", "7", "20", "32", "17",
                "5", "22", "34", "15", "3", "24", "36", "13", "1", "00",
                "27", "10", "25", "29", "12", "8", "19", "31", "18", "6",
                "21", "33", "16", "4", "23", "35", "14", "2"
        };

        for (String val : sequence) {
            PocketColor color;
            if ("0".equals(val) || "00".equals(val)) {
                color = PocketColor.GREEN;
            } else {
                int num = Integer.parseInt(val);
                color = RED_NUMBERS.contains(num) ? PocketColor.RED : PocketColor.BLACK;
            }
            RoulettePocket pocket = new RoulettePocket(val, color);
            pockets.add(pocket);
            pocketMap.put(val, pocket);
        }
    }

    public List<RoulettePocket> getPockets() {
        return Collections.unmodifiableList(pockets);
    }

    public RoulettePocket getPocket(String value) {
        return pocketMap.get(value);
    }

    public RoulettePocket spin() {
        int index = random.nextInt(pockets.size());
        return pockets.get(index);
    }
}
