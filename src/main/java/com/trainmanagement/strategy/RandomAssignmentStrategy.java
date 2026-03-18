package com.trainmanagement.strategy;

import com.trainmanagement.models.Platform;
import com.trainmanagement.repositories.ScheduleRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public class RandomAssignmentStrategy implements PlatformAssignmentStrategy {
    private final Random random = new Random();

    @Override
    public Platform assign(List<Platform> availablePlatforms,
                           LocalDateTime arrivalTime,
                           LocalDateTime departureTime,
                           ScheduleRepository scheduleRepository) {
        int index = random.nextInt(availablePlatforms.size());
        return availablePlatforms.get(index);
    }
}
