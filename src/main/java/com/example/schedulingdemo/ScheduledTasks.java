package com.example.schedulingdemo;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {
    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    // Runs every 5 seconds (measured from method start)
    @Scheduled(fixedRate = 5000)
    public void everyFiveSeconds() {
        log.info("everyFiveSeconds -> {}", LocalDateTime.now());
    }

    // Runs 5 seconds after previous execution finishes
    @Scheduled(fixedDelay = 5000)
    public void fiveSecondsAfterFinish() {
        log.info("fiveSecondsAfterFinish -> {}", LocalDateTime.now());
    }

    // Cron example: every minute (use your timezone or 'zone' attr)
    @Scheduled(cron = "0 * * * * *")
    public void everyMinute() {
        log.info("everyMinute -> {}", LocalDateTime.now());
    }
}
