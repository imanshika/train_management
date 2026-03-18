package com.trainmanagement.observer;

import com.trainmanagement.enums.ScheduleStatus;
import com.trainmanagement.models.Schedule;

public interface ScheduleObserver {
    public void onStatusChange(Schedule schedule, ScheduleStatus oldStatus, ScheduleStatus newStatus);

}
