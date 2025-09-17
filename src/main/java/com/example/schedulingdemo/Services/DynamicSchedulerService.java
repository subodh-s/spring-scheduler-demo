package com.example.schedulingdemo.Services;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

@Service
public class DynamicSchedulerService {
    private static final Logger log = LoggerFactory.getLogger(DynamicSchedulerService.class);

    private final ThreadPoolTaskScheduler scheduler;
    private final Map<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();

    public DynamicSchedulerService(ThreadPoolTaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

    // Schedule a cron task by id
    public boolean scheduleCronTask(String id, String cronExpression) {
        if (tasks.containsKey(id)) {
            log.warn("Task with id {} already exists", id);
            return false;
        }
        Runnable job = () -> log.info("CronTask[{}] executed at {}", id, LocalDateTime.now());
        Trigger trigger = new CronTrigger(cronExpression);
        ScheduledFuture<?> future = scheduler.schedule(job, trigger);
        tasks.put(id, future);
        log.info("Scheduled cron task {} -> {}", id, cronExpression);
        return true;
    }

    // Schedule a fixed-rate task (millis)
    public boolean scheduleFixedRateTask(String id, long periodMillis) {
        if (tasks.containsKey(id)) {
            log.warn("Task with id {} already exists", id);
            return false;
        }
        Runnable job = () -> log.info("FixedRateTask[{}] executed at {}", id, LocalDateTime.now());
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(job, periodMillis);
        tasks.put(id, future);
        log.info("Scheduled fixed-rate task {} -> {}ms", id, periodMillis);
        return true;
    }

    // Cancel a scheduled task (returns true if removed)
    public boolean cancelTask(String id) {
        ScheduledFuture<?> future = tasks.remove(id);
        if (future != null) {
            boolean cancelled = future.cancel(false); // false -> do not interrupt running job
            log.info("Cancelled task {} -> {}", id, cancelled);
            return cancelled;
        }
        return false;
    }

    public Map<String, String> listTasks() {
        Map<String, String> res = new ConcurrentHashMap<>();
        tasks.forEach((k, f) -> res.put(k, f.isCancelled() ? "CANCELLED" : "SCHEDULED"));
        return Collections.unmodifiableMap(res);
    }
}
