package com.example.kyotu.config;

import com.example.kyotu.model.Elevator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class ElevatorsConfig {

    @Bean
    public ConcurrentHashMap<Integer, Elevator> createElevators(PropertiesConfig propertiesConfig){
        System.out.println(propertiesConfig);
        Integer numberOfElevators = propertiesConfig.getNumberOfElevators();
        ConcurrentHashMap<Integer, Elevator> elevatorConcurrentHashMap = new ConcurrentHashMap<>();
        for (int i = 1; i <= numberOfElevators; i++){
            elevatorConcurrentHashMap.put(i, Elevator.create(i));
        }
        return elevatorConcurrentHashMap;
    }

    @Bean
    public ScheduledExecutorService createScheduler(){
        return Executors.newSingleThreadScheduledExecutor();
    }
}
