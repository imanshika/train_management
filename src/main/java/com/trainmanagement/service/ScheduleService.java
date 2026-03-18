package com.trainmanagement.service;

import com.trainmanagement.enums.ScheduleStatus;
import com.trainmanagement.exceptions.PlatformConflictException;
import com.trainmanagement.exceptions.ScheduleNotFoundException;
import com.trainmanagement.models.Platform;
import com.trainmanagement.models.Schedule;
import com.trainmanagement.models.Train;
import com.trainmanagement.observer.ScheduleObserver;
import com.trainmanagement.repositories.ScheduleRepository;
import com.trainmanagement.strategy.PlatformAssignmentStrategy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final TrainService trainService;
    private final PlatformService platformService;
    private final Map<String, ReadWriteLock> platformLocks = new ConcurrentHashMap<>();
    private final List<ScheduleObserver> observers = new CopyOnWriteArrayList<>();

    public ScheduleService(ScheduleRepository scheduleRepository, TrainService trainService, PlatformService platformService) {
        this.scheduleRepository = scheduleRepository;
        this.trainService = trainService;
        this.platformService = platformService;
    }

    // ==================== Observer management ====================

    public void registerObserver(ScheduleObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(ScheduleObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(Schedule schedule, ScheduleStatus oldStatus, ScheduleStatus newStatus) {
        for (ScheduleObserver observer : observers) {
            observer.onStatusChange(schedule, oldStatus, newStatus);
        }
    }

    private ReadWriteLock getLock(String platformId) {
        return platformLocks.computeIfAbsent(platformId, k -> new ReentrantReadWriteLock());
    }

    // ==================== WRITE operations (exclusive lock) ====================

    public Schedule addSchedule(String trainId, String platformId, LocalDateTime arrivalTime, LocalDateTime departureTime) {
        Train train = trainService.getById(trainId);
        Platform platform = platformService.getById(platformId);

        ReadWriteLock lock = getLock(platformId);
        lock.writeLock().lock();
        try {
            checkConflict(platformId, arrivalTime, departureTime, null);
            String id = UUID.randomUUID().toString();
            Schedule schedule = new Schedule(id, train, platform, arrivalTime, departureTime);
            scheduleRepository.save(schedule);
            notifyObservers(schedule, null, ScheduleStatus.SCHEDULED);
            return schedule;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Schedule modifySchedule(String scheduleId, LocalDateTime newArrival, LocalDateTime newDeparture) {
        Schedule schedule = getScheduleById(scheduleId);
        String platformId = schedule.getPlatform().getId();

        ReadWriteLock lock = getLock(platformId);
        lock.writeLock().lock();
        try {
            checkConflict(platformId, newArrival, newDeparture, scheduleId);
            schedule.setArrivalTime(newArrival);
            schedule.setDepartureTime(newDeparture);
            scheduleRepository.save(schedule);
            return schedule;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void cancelSchedule(String scheduleId) {
        Schedule schedule = getScheduleById(scheduleId);
        String platformId = schedule.getPlatform().getId();

        ReadWriteLock lock = getLock(platformId);
        lock.writeLock().lock();
        try {
            ScheduleStatus oldStatus = schedule.getStatus();
            schedule.setStatus(ScheduleStatus.CANCELLED);
            scheduleRepository.save(schedule);
            notifyObservers(schedule, oldStatus, ScheduleStatus.CANCELLED);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void updateStatus(String scheduleId, ScheduleStatus newStatus) {
        Schedule schedule = getScheduleById(scheduleId);
        String platformId = schedule.getPlatform().getId();

        ReadWriteLock lock = getLock(platformId);
        lock.writeLock().lock();
        try {
            ScheduleStatus oldStatus = schedule.getStatus();
            schedule.setStatus(newStatus);
            scheduleRepository.save(schedule);
            notifyObservers(schedule, oldStatus, newStatus);
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ==================== READ operations (shared lock) ====================

    public Schedule getScheduleById(String scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found with id: " + scheduleId));
    }

    public List<Schedule> getSchedulesByPlatform(String platformId) {
        ReadWriteLock lock = getLock(platformId);
        lock.readLock().lock();
        try {
            return scheduleRepository.findByPlatformId(platformId);
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Schedule> getSchedulesByTrain(String trainId) {
        return scheduleRepository.findByTrainId(trainId);
    }

    public Schedule autoScheduleTrain(String trainId, LocalDateTime arrivalTime,
                                       LocalDateTime departureTime,
                                       PlatformAssignmentStrategy strategy) {
        trainService.getById(trainId);
        List<Platform> available = getAvailablePlatforms(arrivalTime, departureTime);
        if (available.isEmpty()) {
            throw new PlatformConflictException("No platforms available between " + arrivalTime + " and " + departureTime);
        }
        Platform selected = strategy.assign(available, arrivalTime, departureTime, scheduleRepository);
        return addSchedule(trainId, selected.getId(), arrivalTime, departureTime);
    }

    public List<Platform> getAvailablePlatforms(LocalDateTime arrivalTime, LocalDateTime departureTime) {
        return platformService.getAllPlatforms().stream()
                .filter(platform -> {
                    ReadWriteLock lock = getLock(platform.getId());
                    lock.readLock().lock();
                    try {
                        return !hasConflict(platform.getId(), arrivalTime, departureTime, null);
                    } finally {
                        lock.readLock().unlock();
                    }
                })
                .collect(Collectors.toList());
    }

    // ==================== Private helpers (called within locked context) ====================

    private void checkConflict(String platformId, LocalDateTime arrivalTime, LocalDateTime departureTime, String excludeScheduleId) {
        if (hasConflict(platformId, arrivalTime, departureTime, excludeScheduleId)) {
            throw new PlatformConflictException(
                    "Platform " + platformId + " is occupied between " + arrivalTime + " and " + departureTime);
        }
    }

    private boolean hasConflict(String platformId, LocalDateTime arrivalTime, LocalDateTime departureTime, String excludeScheduleId) {
        List<Schedule> activeSchedules = scheduleRepository.findActiveByPlatformId(platformId);
        return activeSchedules.stream()
                .filter(s -> !s.getId().equals(excludeScheduleId))
                .anyMatch(existing ->
                        arrivalTime.isBefore(existing.getDepartureTime())
                                && existing.getArrivalTime().isBefore(departureTime));
    }
}
