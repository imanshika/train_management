package com.trainmanagement.repositories;

import com.trainmanagement.models.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class PlatformRepository {
    private final Map<String, Platform> platforms = new ConcurrentHashMap<>();

    public void save(Platform platform) {
        platforms.put(platform.getId(), platform);
    }

    public Optional<Platform> findById(String id) {
        return Optional.ofNullable(platforms.get(id));
    }

    public List<Platform> findAll() {
        return new ArrayList<>(platforms.values());
    }

    public boolean existsById(String id) {
        return platforms.containsKey(id);
    }

    public Optional<Platform> deleteById(String id) {
        return Optional.ofNullable(platforms.remove(id));
    }
}
