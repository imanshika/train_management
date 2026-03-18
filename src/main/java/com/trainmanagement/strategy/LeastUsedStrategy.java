package com.trainmanagement.strategy;

import com.trainmanagement.models.Platform;
import com.trainmanagement.repositories.ScheduleRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class LeastUsedStrategy implements PlatformAssignmentStrategy {

    @Override
    public Platform assign(List<Platform> availablePlatforms,
                           LocalDateTime arrivalTime,
                           LocalDateTime departureTime,
                           ScheduleRepository scheduleRepository) {
        return availablePlatforms.stream()
                .min(Comparator.comparingInt(p -> scheduleRepository.findByPlatformId(p.getId()).size()))
                .orElseThrow(() -> new RuntimeException("No available platforms"));
    }
}
