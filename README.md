# Spring Scheduler Demo

**Spring Scheduler Demo** — a minimal Spring Boot project demonstrating different scheduling approaches:
- `@Scheduled` examples (`fixedRate`, `fixedDelay`, `cron`)
- Custom `ThreadPoolTaskScheduler` used for scheduled tasks (pool size configured)
- A small REST API to add/cancel/list programmatic/dynamic scheduled tasks

This repo is intended as a demo / internal practice project and as the companion repo for a Medium article about Spring Scheduler.

---

## Quick facts

- **Java**: 17  
- **Spring Boot**: 3.5.5
- **Build tool**: Maven (project includes `mvnw` wrapper)  
- **Main class**: `com.example.schedulingdemo.SchedulingDemoApplication` (has `@EnableScheduling`)

---

## What’s in this demo

### 1) `ScheduledTasks` (`com.example.schedulingdemo.ScheduledTasks`)
This component demonstrates three scheduled methods:

```java
// Runs every 5 seconds (measured from method start)
@Scheduled(fixedRate = 5000)
public void everyFiveSeconds() { ... }

// Runs 5 seconds after previous execution finishes
@Scheduled(fixedDelay = 5000)
public void fiveSecondsAfterFinish() { ... }

// Cron example: every minute
@Scheduled(cron = "0 * * * * *")
public void everyMinute() { ... }
```

Each method logs its name and `LocalDateTime` when executed.

---

### 2) `SchedulingConfig` (`com.example.schedulingdemo.SchedulingConfig`)
This config registers a `ThreadPoolTaskScheduler` with pool size **10** and sets the thread name prefix to `dyn-sched-`. It implements `SchedulingConfigurer` and calls:

```java
threadPoolTaskScheduler.setPoolSize(10);
threadPoolTaskScheduler.setThreadNamePrefix("dyn-sched-");
taskRegistrar.setScheduler(this.threadPoolTaskScheduler);
```

Because this pool is registered in `ScheduledTaskRegistrar`, both `@Scheduled` methods and programmatic scheduling share the pool (i.e., multi-threaded scheduling).

---

### 3) Dynamic scheduling REST API
A small controller exposes endpoints to manage scheduled tasks at runtime:

**Base:** `POST /api/scheduler`

- `POST /api/scheduler/cron?id=<id>&cron=<cronExpr>`  
  Add a cron-based scheduled task (returns 200 OK or 400 if id exists).

- `POST /api/scheduler/fixed?id=<id>&periodMillis=<milliseconds>`  
  Add a fixed-rate task with specified period in milliseconds.

- `DELETE /api/scheduler/{id}`  
  Cancel a scheduled task by ID.

- `GET /api/scheduler/list`  
  Returns a map of taskIds -> `SCHEDULED` | `CANCELLED`.

The service backing these endpoints is `com.example.schedulingdemo.Services.DynamicSchedulerService` (programmatic scheduling with `ThreadPoolTaskScheduler` and `CronTrigger`).


---

## Build & Run

Clone & build:

```bash
git clone https://github.com/subodh-s/spring-scheduler-demo.git
cd spring-scheduler-demo

# with wrapper
./mvnw clean package

# run
./mvnw spring-boot:run

# or run jar
java -jar target/scheduling-demo-0.0.1-SNAPSHOT.jar
```

When the app runs you should see log lines for the scheduled `everyFiveSeconds`, `fiveSecondsAfterFinish`, and `everyMinute` tasks.

---

## Sample log (what to expect)

```
2025-09-17 14:00:05 INFO  ... ScheduledTasks - everyFiveSeconds -> 2025-09-17T14:00:05
2025-09-17 14:00:07 INFO  ... ScheduledTasks - fiveSecondsAfterFinish -> 2025-09-17T14:00:07
2025-09-17 14:01:00 INFO  ... ScheduledTasks - everyMinute -> 2025-09-17T14:01:00
```

Programmatic scheduling (dynamic tasks) will log created/cancelled tasks (the controller/service call logs are present if implemented).

---

## How to demo the dynamic endpoints (curl examples)

1. Add a fixed-rate task that runs every 10 seconds:
```bash
curl -X POST "http://localhost:8080/api/scheduler/fixed?id=myFixed&periodMillis=10000"
```

2. Add a cron task that runs every minute:
```bash
curl -X POST "http://localhost:8080/api/scheduler/cron?id=myCron&cron=0 * * * * *"
```

3. List tasks:
```bash
curl "http://localhost:8080/api/scheduler/list" | jq
```

4. Cancel a task:
```bash
curl -X DELETE "http://localhost:8080/api/scheduler/myCron"
```

---
