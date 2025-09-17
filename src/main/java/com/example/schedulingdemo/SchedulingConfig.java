package com.example.schedulingdemo;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
public class SchedulingConfig implements SchedulingConfigurer {

    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;

    public SchedulingConfig() {
        this.threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        this.threadPoolTaskScheduler.setPoolSize(10);
        this.threadPoolTaskScheduler.setThreadNamePrefix("dyn-sched-");
        this.threadPoolTaskScheduler.initialize();
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        // register our pool so @Scheduled (if used) and programmatic tasks share it
        taskRegistrar.setScheduler(this.threadPoolTaskScheduler);
    }
}
