package com.trainmanagement.repositories;

import com.trainmanagement.enums.ScheduleStatus;
import com.trainmanagement.models.Schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ScheduleRepository {
    private final Map<String, Schedule> schedules = new ConcurrentHashMap<>();

    public void save(Schedule schedule) {
        schedules.put(schedule.getId(), schedule);
    }

    public Optional<Schedule> findById(String id) {
        return Optional.ofNullable(schedules.get(id));
    }

    public List<Schedule> findAll() {
        return new ArrayList<>(schedules.values());
    }

    public List<Schedule> findByPlatformId(String platformId) {
        return schedules.values().stream()
                .filter(s -> s.getPlatform().getId().equals(platformId))
                .collect(Collectors.toList());
    }

    public List<Schedule> findByTrainId(String trainId) {
        return schedules.values().stream()
                .filter(s -> s.getTrain().getId().equals(trainId))
                .collect(Collectors.toList());
    }

    public List<Schedule> findActiveByPlatformId(String platformId) {
        return schedules.values().stream()
                .filter(s -> s.getPlatform().getId().equals(platformId))
                .filter(s -> s.getStatus() != ScheduleStatus.CANCELLED
                          && s.getStatus() != ScheduleStatus.DEPARTED)
                .collect(Collectors.toList());
    }

    public boolean existsById(String id) {
        return schedules.containsKey(id);
    }

    public Optional<Schedule> deleteById(String id) {
        return Optional.ofNullable(schedules.remove(id));
    }
}
