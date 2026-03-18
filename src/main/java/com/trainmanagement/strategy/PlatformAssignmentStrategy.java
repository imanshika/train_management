package com.trainmanagement.strategy;

import com.trainmanagement.models.Platform;
import com.trainmanagement.repositories.ScheduleRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PlatformAssignmentStrategy {
    Platform assign(List<Platform> availablePlatforms,
                    LocalDateTime arrivalTime,
                    LocalDateTime departureTime,
                    ScheduleRepository scheduleRepository);
}
