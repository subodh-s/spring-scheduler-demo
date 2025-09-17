package com.example.schedulingdemo.Controllers;

import java.util.Map;

import com.example.schedulingdemo.Services.DynamicSchedulerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scheduler")
public class SchedulerController {
    private final DynamicSchedulerService schedulerService;

    public SchedulerController(DynamicSchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @PostMapping("/cron")
    public ResponseEntity<String> scheduleCron(@RequestParam String id, @RequestParam String cron) {
        boolean ok = schedulerService.scheduleCronTask(id, cron);
        return ok ? ResponseEntity.ok("Scheduled cron task " + id) : ResponseEntity.badRequest().body("Task exists");
    }

    @PostMapping("/fixed")
    public ResponseEntity<String> scheduleFixed(@RequestParam String id, @RequestParam long periodMillis) {
        boolean ok = schedulerService.scheduleFixedRateTask(id, periodMillis);
        return ok ? ResponseEntity.ok("Scheduled fixed-rate task " + id)
                : ResponseEntity.badRequest().body("Task exists");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> cancel(@PathVariable String id) {
        boolean cancelled = schedulerService.cancelTask(id);
        return cancelled ? ResponseEntity.ok("Cancelled " + id) : ResponseEntity.notFound().build();
    }

    @GetMapping("/list")
    public Map<String, String> list() {
        return schedulerService.listTasks();
    }
}

