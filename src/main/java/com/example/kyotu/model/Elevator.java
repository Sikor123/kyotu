package com.example.kyotu.model;

import lombok.Builder;
import lombok.Data;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Data
@Builder
public class Elevator {
    private int id;
    private long currentPosition;
    private long desiredPosition;
    private long waitingTime;
    private ElevatorState state;
    private BlockingQueue<Integer> elevatorOrdersQueue;
    private boolean doorsOpen;

    public static Elevator create(int id) {
        return Elevator.builder()
                .id(id)
                .currentPosition(0)
                .state(ElevatorState.IDLE)
                .elevatorOrdersQueue(new ArrayBlockingQueue<>(1024))
                .doorsOpen(false)
                .build();
    }
}

