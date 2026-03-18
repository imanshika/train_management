package com.trainmanagement.observer;

import com.trainmanagement.enums.ScheduleStatus;
import com.trainmanagement.models.Schedule;

public class CleanupCrewObserver implements ScheduleObserver {

    @Override
    public void onStatusChange(Schedule schedule, ScheduleStatus oldStatus, ScheduleStatus newStatus) {
        if (newStatus == ScheduleStatus.DEPARTED) {
            System.out.println("[NOTIFY] " + schedule.getTrain().getName() + " has DEPARTED from Platform " + schedule.getPlatform().getPlatformNumber() + " at " + schedule.getDepartureTime());
        }
    }
}
