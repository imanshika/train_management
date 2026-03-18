package com.trainmanagement.observer;

import com.trainmanagement.enums.ScheduleStatus;
import com.trainmanagement.models.Schedule;

public class PassengerNotificationObserver implements ScheduleObserver {

    @Override
    public void onStatusChange(Schedule schedule, ScheduleStatus oldStatus, ScheduleStatus newStatus) {
        if (newStatus == ScheduleStatus.ARRIVED) {
            System.out.println("[ALERT] " + schedule.getTrain().getName() + " has ARRIVED at Platform " + schedule.getPlatform().getPlatformNumber());
        } else if (newStatus == ScheduleStatus.CANCELLED) {
            System.out.println("[ALERT] " + schedule.getTrain().getName() + " scheduled on Platform " + schedule.getPlatform().getPlatformNumber() + " is CANCELLED");
        }
    }
}
