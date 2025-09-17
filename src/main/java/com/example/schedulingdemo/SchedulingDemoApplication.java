package com.example.schedulingdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SchedulingDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulingDemoApplication.class, args);
    }

}
