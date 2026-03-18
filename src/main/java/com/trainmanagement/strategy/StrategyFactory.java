package com.trainmanagement.strategy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StrategyFactory {
    private static final Map<String, PlatformAssignmentStrategy> strategies = new ConcurrentHashMap<>();

    static {
        strategies.put("FIRST_AVAILABLE", new FirstAvailableStrategy());
        strategies.put("LEAST_USED", new LeastUsedStrategy());
        strategies.put("RANDOM", new RandomAssignmentStrategy());
    }

    public static PlatformAssignmentStrategy getStrategy(String type) {
        PlatformAssignmentStrategy strategy = strategies.get(type.toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown strategy: " + type);
        }
        return strategy;
    }

    public static void registerStrategy(String type, PlatformAssignmentStrategy strategy) {
        strategies.put(type.toUpperCase(), strategy);
    }
}
