package com.trainmanagement.observer;

import com.trainmanagement.enums.ScheduleStatus;
import com.trainmanagement.models.Schedule;

public class DisplayBoardObserver implements ScheduleObserver {
    @Override
    public void onStatusChange(Schedule schedule, ScheduleStatus oldStatus, ScheduleStatus newStatus) {
        if(newStatus == ScheduleStatus.SCHEDULED) {
            System.out.println("[DISPLAY] " + schedule.getTrain().getName() + " SCHEDULED to depart at " + schedule.getDepartureTime() + " on Platform " + schedule.getPlatform().getPlatformNumber());
        } else if (newStatus == ScheduleStatus.ARRIVED) {
            System.out.println("[DISPLAY] " + schedule.getTrain().getName() + " has ARRIVED at Platform " + schedule.getPlatform().getPlatformNumber());
        } else if (newStatus == ScheduleStatus.DEPARTED) {
            System.out.println("[DISPLAY] " + schedule.getTrain().getName() + " has DEPARTED from Platform " + schedule.getPlatform().getPlatformNumber() + " at " + schedule.getDepartureTime());
        } else if (newStatus == ScheduleStatus.CANCELLED) {
            System.out.println("[DISPLAY] " + schedule.getTrain().getName() + " scheduled on Platform " + schedule.getPlatform().getPlatformNumber() + " is CANCELLED");
        }
    }
}
