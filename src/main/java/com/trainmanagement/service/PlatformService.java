package com.trainmanagement.service;

import com.trainmanagement.exceptions.PlatformNotFoundException;
import com.trainmanagement.models.Platform;
import com.trainmanagement.repositories.PlatformRepository;

import java.util.List;
import java.util.UUID;

public class PlatformService {
    PlatformRepository platformRepository;

    public PlatformService(PlatformRepository platformRepository) {
        this.platformRepository = platformRepository;
    }

    public Platform addPlatform(int platformNumber, String stationName) {
        String id = UUID.randomUUID().toString();
        Platform platform = new Platform(id, platformNumber, stationName);
        platformRepository.save(platform);
        return platform;
    }

    public List<Platform> getAllPlatforms() {
        return platformRepository.findAll();
    }

    public Platform removePlatform(String id) {
        return platformRepository.deleteById(id)
                .orElseThrow(() -> new PlatformNotFoundException("Platform not found with id: " + id));
    }

    public Platform getById(String id){
        return platformRepository.findById(id).orElseThrow(() -> new PlatformNotFoundException("Platform not found with id: " + id));
    }
}
