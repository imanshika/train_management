package org.example;

import com.trainmanagement.enums.ScheduleStatus;
import com.trainmanagement.enums.TrainType;
import com.trainmanagement.exceptions.PlatformConflictException;
import com.trainmanagement.models.Platform;
import com.trainmanagement.models.Schedule;
import com.trainmanagement.models.Train;
import com.trainmanagement.repositories.PlatformRepository;
import com.trainmanagement.repositories.ScheduleRepository;
import com.trainmanagement.repositories.TrainRepository;
import com.trainmanagement.service.PlatformService;
import com.trainmanagement.service.ScheduleService;
import com.trainmanagement.service.TrainService;

import com.trainmanagement.observer.CleanupCrewObserver;
import com.trainmanagement.observer.DisplayBoardObserver;
import com.trainmanagement.observer.PassengerNotificationObserver;
import com.trainmanagement.strategy.StrategyFactory;

import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        TrainRepository trainRepository = new TrainRepository();
        PlatformRepository platformRepository = new PlatformRepository();
        ScheduleRepository scheduleRepository = new ScheduleRepository();

        TrainService trainService = new TrainService(trainRepository);
        PlatformService platformService = new PlatformService(platformRepository);
        ScheduleService scheduleService = new ScheduleService(scheduleRepository, trainService, platformService);

        // --- Register Observers ---
        scheduleService.registerObserver(new DisplayBoardObserver());
        scheduleService.registerObserver(new PassengerNotificationObserver());
        scheduleService.registerObserver(new CleanupCrewObserver());

        // --- Add Trains ---
        Train t1 = trainService.addTrain("TRAIN-1234", "Rajdhani Express", TrainType.EXPRESS);
        Train t2 = trainService.addTrain("TRAIN-5678", "Shatabdi Express", TrainType.SUPERFAST);
        Train t3 = trainService.addTrain("TRAIN-9101", "Duronto Express", TrainType.EXPRESS);
        Train t4 = trainService.addTrain("TRAIN-1122", "Mumbai Local", TrainType.LOCAL);
        Train t5 = trainService.addTrain("TRAIN-3344", "Goods Carrier", TrainType.FREIGHT);
        System.out.println("=== Trains Added ===");
        System.out.println(t1);
        System.out.println(t2);
        System.out.println(t3);
        System.out.println(t4);
        System.out.println(t5);

        // --- Add Platforms ---
        Platform p1 = platformService.addPlatform(1, "New Delhi");
        Platform p2 = platformService.addPlatform(2, "New Delhi");
        Platform p3 = platformService.addPlatform(3, "New Delhi");
        Platform p4 = platformService.addPlatform(4, "New Delhi");
        System.out.println("\n=== Platforms Added ===");
        System.out.println(p1);
        System.out.println(p2);
        System.out.println(p3);
        System.out.println(p4);

        // --- Schedule Trains on Platforms ---
        LocalDateTime now = LocalDateTime.now();
        Schedule s1 = scheduleService.addSchedule(t1.getId(), p1.getId(), now, now.plusHours(10));
        Schedule s2 = scheduleService.addSchedule(t2.getId(), p2.getId(), now, now.plusHours(15));
        Schedule s3 = scheduleService.addSchedule(t3.getId(), p3.getId(), now, now.plusHours(11));
        Schedule s4 = scheduleService.addSchedule(t4.getId(), p4.getId(), now, now.plusHours(10));
        System.out.println("\n=== Schedules Created ===");
        System.out.println(s1);
        System.out.println(s2);
        System.out.println(s3);
        System.out.println(s4);

        // --- Conflict Detection ---
        System.out.println("\n=== Conflict Detection Test ===");
        try {
            scheduleService.addSchedule(t5.getId(), p1.getId(), now.plusHours(2), now.plusHours(3));
            System.out.println("ERROR: Should have thrown conflict!");
        } catch (PlatformConflictException e) {
            System.out.println("Caught expected conflict: " + e.getMessage());
        }

        // --- Query: Schedules by Platform ---
        System.out.println("\n=== Schedules on Platform 1 ===");
        List<Schedule> platformSchedules = scheduleService.getSchedulesByPlatform(p1.getId());
        platformSchedules.forEach(System.out::println);

        // --- Query: Schedules by Train ---
        System.out.println("\n=== Schedules for Rajdhani Express ===");
        List<Schedule> trainSchedules = scheduleService.getSchedulesByTrain(t1.getId());
        trainSchedules.forEach(System.out::println);

        // --- Find Available Platforms ---
        System.out.println("\n=== Available Platforms (now+12h to now+14h) ===");
        List<Platform> available = scheduleService.getAvailablePlatforms(now.plusHours(12), now.plusHours(14));
        available.forEach(System.out::println);

        // --- Update Status: Arrived then Departed ---
        System.out.println("\n=== Status Updates ===");
        scheduleService.updateStatus(s1.getId(), ScheduleStatus.ARRIVED);
        System.out.println("After ARRIVED: " + scheduleService.getScheduleById(s1.getId()));
        scheduleService.updateStatus(s1.getId(), ScheduleStatus.DEPARTED);
        System.out.println("After DEPARTED: " + scheduleService.getScheduleById(s1.getId()));

        // --- Cancel Schedule ---
        System.out.println("\n=== Cancel Schedule ===");
        scheduleService.cancelSchedule(s3.getId());
        System.out.println("Cancelled: " + scheduleService.getScheduleById(s3.getId()));

        // --- After departure/cancel, platform 1 and 3 should be free ---
        System.out.println("\n=== Available Platforms (now to now+5h) after departure & cancel ===");
        List<Platform> nowAvailable = scheduleService.getAvailablePlatforms(now, now.plusHours(5));
        nowAvailable.forEach(System.out::println);

        // --- Modify Schedule ---
        System.out.println("\n=== Modify Schedule ===");
        Schedule modified = scheduleService.modifySchedule(s4.getId(), now.plusHours(1), now.plusHours(5));
        System.out.println("Modified: " + modified);

        // --- Auto-Assign: FirstAvailable Strategy (via Factory) ---
        System.out.println("\n=== Auto-Assign: FirstAvailable Strategy ===");
        Schedule autoFirst = scheduleService.autoScheduleTrain(
                t5.getId(), now.plusHours(20), now.plusHours(22), StrategyFactory.getStrategy("FIRST_AVAILABLE"));
        System.out.println("Auto-assigned (FirstAvailable): " + autoFirst);

        // --- Auto-Assign: LeastUsed Strategy (via Factory) ---
        System.out.println("\n=== Auto-Assign: LeastUsed Strategy ===");
        Train t6 = trainService.addTrain("TRAIN-5566", "Garib Rath", TrainType.EXPRESS);
        Schedule autoLeast = scheduleService.autoScheduleTrain(
                t6.getId(), now.plusHours(20), now.plusHours(22), StrategyFactory.getStrategy("LEAST_USED"));
        System.out.println("Auto-assigned (LeastUsed): " + autoLeast);

        // --- Auto-Assign: Random Strategy (via Factory) ---
        System.out.println("\n=== Auto-Assign: Random Strategy ===");
        Train t7 = trainService.addTrain("TRAIN-7788", "Deccan Queen", TrainType.SUPERFAST);
        Schedule autoRandom = scheduleService.autoScheduleTrain(
                t7.getId(), now.plusHours(20), now.plusHours(22), StrategyFactory.getStrategy("RANDOM"));
        System.out.println("Auto-assigned (Random): " + autoRandom);

        // --- Remove Train ---
        System.out.println("\n=== Remove Train ===");
        Train removed = trainService.removeTrain(t5.getId());
        System.out.println("Removed: " + removed);
    }
}
