# Train Platform Management System

A low-level design (LLD) / machine coding implementation for managing train-to-platform assignments at a railway station, with conflict detection, scheduling, strategy-based auto-assignment, and event-driven notifications.

## Problem Statement

Design a system that manages train arrivals and departures across multiple platforms at a station. The system must ensure no two trains are assigned to the same platform during overlapping time windows.

## Entity Models

### Train
| Field       | Type   | Description                        |
|-------------|--------|------------------------------------|
| id          | String | Unique identifier (UUID)           |
| trainNumber | String | Official train number (e.g. 12345) |
| name        | String | Train name (e.g. Rajdhani Express) |
| trainType   | Enum   | EXPRESS, SUPERFAST, LOCAL, FREIGHT |

### Platform
| Field          | Type   | Description                      |
|----------------|--------|----------------------------------|
| id             | String | Unique identifier (UUID)         |
| platformNumber | int    | Display number (1, 2, 3...)      |
| stationName    | String | Station this platform belongs to |

### Schedule
| Field         | Type          | Description                              |
|---------------|---------------|------------------------------------------|
| id            | String        | Unique identifier (UUID)                 |
| train         | Train         | Reference to the assigned train          |
| platform      | Platform      | Reference to the assigned platform       |
| arrivalTime   | LocalDateTime | When the train arrives at the platform   |
| departureTime | LocalDateTime | When the train departs from the platform |
| status        | Enum          | SCHEDULED, ARRIVED, DEPARTED, CANCELLED  |

## Core Functionalities

| #  | Feature                    | Description                                              |
|----|----------------------------|----------------------------------------------------------|
| 1  | Add Train                  | Register a new train in the system                       |
| 2  | Add Platform               | Register a new platform at a station                     |
| 3  | Schedule Train on Platform | Assign a train to a platform for a given time window     |
| 4  | Conflict Detection         | Reject scheduling if the platform is occupied            |
| 5  | Get Platform Schedule      | List all trains scheduled on a specific platform         |
| 6  | Get Train Schedule         | List all platform assignments for a specific train       |
| 7  | Modify / Cancel Schedule   | Update arrival/departure times or cancel a schedule      |
| 8  | Find Available Platforms   | Given a time window, return all platforms that are free   |
| 9  | Update Schedule Status     | Transition status: SCHEDULED → ARRIVED → DEPARTED        |
| 10 | Auto-Assign Platform       | System picks a platform using a pluggable strategy       |
| 11 | Remove Train / Platform    | Delete entities from the system                          |
| 12 | Event Notifications        | Observers notified on every schedule status change       |

## Design Patterns Used

### 1. Strategy Pattern — Auto Platform Assignment

When a train needs a platform but doesn't specify which one, the system auto-assigns using a pluggable strategy:

```
              <<interface>>
          PlatformAssignmentStrategy
          ─────────────────────────
          + assign(platforms, arrival, departure, scheduleRepo) → Platform
                    │
       ┌────────────┼────────────────┐
       │            │                │
FirstAvailable  LeastUsed   RandomAssignment
```

| Strategy                  | Logic                                      | Best For                          |
|---------------------------|--------------------------------------------|------------------------------------|
| `FirstAvailableStrategy`  | Picks lowest platform number               | Simple default, predictable        |
| `LeastUsedStrategy`       | Picks platform with fewest total schedules | Even load distribution             |
| `RandomAssignmentStrategy`| Picks a random available platform          | Load balancing without bias        |

### 2. Factory Pattern — Strategy Creation

`StrategyFactory` decouples strategy selection from instantiation. Strategies are looked up by name instead of using `new` directly:

```java
StrategyFactory.getStrategy("FIRST_AVAILABLE")   // returns FirstAvailableStrategy
StrategyFactory.getStrategy("LEAST_USED")         // returns LeastUsedStrategy
StrategyFactory.getStrategy("RANDOM")             // returns RandomAssignmentStrategy
StrategyFactory.registerStrategy("CUSTOM", new MyStrategy())  // extensible at runtime
```

### 3. Observer Pattern — Event Notifications

When a schedule status changes, all registered observers are notified automatically:

```
ScheduleService (Publisher)
  │  updateStatus()
  │  cancelSchedule()
  │  addSchedule()
  │
  └── notifyObservers(schedule, oldStatus, newStatus)
            │
            ├── DisplayBoardObserver           → [DISPLAY] logs every status change
            ├── PassengerNotificationObserver   → [ALERT] notifies on ARRIVED, CANCELLED
            └── CleanupCrewObserver             → [NOTIFY] triggers on DEPARTED only
```

Observers are registered externally (not in the constructor), keeping `ScheduleService` decoupled from concrete observer implementations.

## Concurrency

Thread safety is handled at multiple levels:

| Layer | Mechanism | Purpose |
|-------|-----------|---------|
| Repositories | `ConcurrentHashMap` | Thread-safe individual read/write operations |
| ScheduleService | `ReadWriteLock` per platform | Prevents check-then-act race condition in scheduling |
| Observer list | `CopyOnWriteArrayList` | Safe iteration during notifications while allowing concurrent registration |

**ReadWriteLock strategy:**
- **Write lock** (exclusive) — `addSchedule`, `modifySchedule`, `cancelSchedule`, `updateStatus`
- **Read lock** (shared, concurrent) — `getSchedulesByPlatform`, `getAvailablePlatforms`
- Locks are **per-platform** — operations on different platforms don't block each other

## Key Design Decisions

- **In-memory storage** — `ConcurrentHashMap` backed repositories for thread-safe reads/writes.
- **Conflict detection** — two schedules conflict when they share the same platform and their time windows overlap. Only active schedules (SCHEDULED, ARRIVED) are considered; CANCELLED and DEPARTED are excluded.
- **Time overlap formula** — Schedule A and B overlap if: `A.arrival < B.departure AND B.arrival < A.departure`.
- **UUID-based IDs** — generated in the service layer using `UUID.randomUUID()`.
- **Unchecked exceptions** — all custom exceptions extend `RuntimeException` to keep service signatures clean.
- **Strategy pattern** — for auto platform assignment, allowing new assignment policies without modifying existing code (Open/Closed Principle).
- **Factory pattern** — for strategy creation, decoupling selection from instantiation.
- **Observer pattern** — for event-driven notifications, decoupling the publisher from subscribers.
- **Per-platform locking** — `ReadWriteLock` per platform for fine-grained concurrency control.

## Custom Exceptions

| Exception                    | Thrown When                                       |
|------------------------------|---------------------------------------------------|
| `TrainNotFoundException`     | Train ID doesn't exist                            |
| `PlatformNotFoundException`  | Platform ID doesn't exist                         |
| `ScheduleNotFoundException`  | Schedule ID doesn't exist                         |
| `PlatformConflictException`  | Time window overlaps with an existing schedule    |

## Project Structure

```
src/main/java/
├── org/example/
│   └── Main.java                              # Entry point / driver
└── com/trainmanagement/
    ├── enums/
    │   ├── TrainType.java                     # EXPRESS, SUPERFAST, LOCAL, FREIGHT
    │   └── ScheduleStatus.java                # SCHEDULED, ARRIVED, DEPARTED, CANCELLED
    ├── models/
    │   ├── Train.java
    │   ├── Platform.java
    │   └── Schedule.java
    ├── repositories/
    │   ├── TrainRepository.java               # ConcurrentHashMap<String, Train>
    │   ├── PlatformRepository.java            # ConcurrentHashMap<String, Platform>
    │   └── ScheduleRepository.java            # ConcurrentHashMap<String, Schedule>
    ├── service/
    │   ├── TrainService.java                  # CRUD for trains
    │   ├── PlatformService.java               # CRUD for platforms
    │   └── ScheduleService.java               # Scheduling, conflict detection, auto-assign, notifications
    ├── strategy/
    │   ├── PlatformAssignmentStrategy.java    # Strategy interface
    │   ├── FirstAvailableStrategy.java        # Lowest platform number
    │   ├── LeastUsedStrategy.java             # Fewest total schedules
    │   ├── RandomAssignmentStrategy.java      # Random pick
    │   └── StrategyFactory.java               # Factory for strategy lookup
    ├── observer/
    │   ├── ScheduleObserver.java              # Observer interface
    │   ├── DisplayBoardObserver.java          # Logs all status changes
    │   ├── PassengerNotificationObserver.java # Alerts on ARRIVED, CANCELLED
    │   └── CleanupCrewObserver.java           # Triggers on DEPARTED
    └── exceptions/
        ├── TrainNotFoundException.java
        ├── PlatformNotFoundException.java
        ├── ScheduleNotFoundException.java
        └── PlatformConflictException.java
```

## Dependency Flow

```
ScheduleService  ──→  ScheduleRepository
       │
       ├──→  TrainService      ──→  TrainRepository
       ├──→  PlatformService   ──→  PlatformRepository
       ├──→  PlatformAssignmentStrategy (injected per call via StrategyFactory)
       └──→  List<ScheduleObserver> (registered externally)
```
