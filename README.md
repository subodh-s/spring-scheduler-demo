# Spring Scheduler Demo

**Spring Scheduler Demo** — a minimal Spring Boot project demonstrating different scheduling approaches:
- `@Scheduled` examples (`fixedRate`, `fixedDelay`, `cron`)
- Custom `ThreadPoolTaskScheduler` used for scheduled tasks (pool size configured)
- A small REST API to add/cancel/list programmatic/dynamic scheduled tasks

This repo is intended as a demo / internal practice project and as the companion repo for a Medium article about Spring Scheduler.

---

## Quick facts

- **Java**: 17  
- **Spring Boot**: 3.5.5 (from `pom.xml`)  
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

> Note: in the uploaded zip the `DynamicSchedulerService` file contains the scheduling logic skeleton (the current repository version in this archive includes the necessary imports, the scheduler field and a `tasks` map). If you intend to publish the repo publicly, ensure the service contains the full implementations of schedule/cancel/list methods (the controller expects these to be present).

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

## Notes, caveats & suggestions for the Medium article

- **Thread pool**: You already configure a `ThreadPoolTaskScheduler` with pool size 10 — mention in the article why the default single-threaded scheduler can be a problem (long-running tasks blocking others) and how a pool fixes that.
- **Overlap prevention**: If a scheduled job may run longer than its schedule, show approaches:
  - Use a lock/flag inside job to skip overlapping runs, or
  - Use `@Scheduled` + `@Async` with a pool, OR
  - Use external distributed locks (e.g., [ShedLock](https://github.com/lukas-krecan/ShedLock)) for multi-node deployments to ensure a job runs only once cluster-wide.
- **Externalize cron/fixed values**: Demonstrate reading cron/fixedRate from `application.properties` so readers can change schedules without code changes:
  ```properties
  scheduler.fixedRateMs=5000
  scheduler.cron.everyMinute=0 * * * * *
  ```
  then inject via `@Value` or `@ConfigurationProperties`.
- **Observability**: Add Actuator + Micrometer metrics for job execution times and counts. Show how to expose a `/actuator/metrics` metric for scheduled task timings.
- **Error handling & retries**: Show how to handle exceptions in scheduled tasks and optionally add retry logic or resilient patterns.
- **Testing**: Add unit/integration tests for scheduling using Spring's `TestTaskScheduler` or by injecting `TaskScheduler` and advancing time.

---

## Suggested README additions (for a published demo)
- Add small example output snapshot (logs) from one run (you can paste console output).
- Add a short section that explains the dynamic scheduling API design and include example responses.
- Add a short diagram (optional) showing scheduler thread pool vs single-threaded scheduler behavior.
- Add a `LICENSE` if you want to publish publicly (MIT is common for demos).

---

## Files used to infer this README

- `pom.xml`: Spring Boot parent version `3.5.5`, `java.version` = `17`.
- `src/main/java/com/example/schedulingdemo/SchedulingDemoApplication.java` (`@EnableScheduling`).
- `src/main/java/com/example/schedulingdemo/ScheduledTasks.java` (`@Scheduled` methods).
- `src/main/java/com/example/schedulingdemo/SchedulingConfig.java` (ThreadPoolTaskScheduler with pool size 10).
- `src/main/java/com/example/schedulingdemo/Controllers/SchedulerController.java` (dynamic endpoints).
- `src/main/java/com/example/schedulingdemo/Services/DynamicSchedulerService.java` (programmatic scheduler service — make sure this file contains the intended implementation if you plan to publish).
