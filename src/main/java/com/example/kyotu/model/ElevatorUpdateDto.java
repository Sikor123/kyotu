package com.example.kyotu.model;

public record ElevatorUpdateDto(
        int elevatorId,
        Long currentPosition,
        ElevatorState elevatorState
) {
}
