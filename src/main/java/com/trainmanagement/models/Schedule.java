package com.trainmanagement.models;

import com.trainmanagement.enums.ScheduleStatus;
import java.time.LocalDateTime;

public class Schedule {
    private final String id;
    private Train train;
    private Platform platform;
    private LocalDateTime arrivalTime;
    private LocalDateTime departureTime;
    private ScheduleStatus status;

    public Schedule(String id, Train train, Platform platform,
                    LocalDateTime arrivalTime, LocalDateTime departureTime) {
        this.id = id;
        this.train = train;
        this.platform = platform;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.status = ScheduleStatus.SCHEDULED;
    }

    public boolean overlapsWith(Schedule other) {
        if (!this.platform.getId().equals(other.platform.getId())) {
            return false;
        }
        return this.arrivalTime.isBefore(other.departureTime)
                && other.arrivalTime.isBefore(this.departureTime);
    }

    public String getId() {
        return id;
    }

    public Train getTrain() {
        return train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public ScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduleStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "id='" + id + '\'' +
                ", train=" + train.getName() +
                ", platform=" + platform.getPlatformNumber() +
                ", arrival=" + arrivalTime +
                ", departure=" + departureTime +
                ", status=" + status +
                '}';
    }
}
