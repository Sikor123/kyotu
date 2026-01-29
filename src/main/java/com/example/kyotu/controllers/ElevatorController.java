package com.example.kyotu.controllers;

import com.example.kyotu.service.ElevatorService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.ACCEPTED;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/elevators")
public class ElevatorController {
    private final ElevatorService elevatorService;

    @PostMapping("/call/{floorNumber}")
    public ResponseEntity<Void> callElevator(@PathVariable("floorNumber") int floorNumber) {
        log.info("Elevator requested on {} floor", floorNumber);
        elevatorService.createRequestOutsideElevator(floorNumber);
        return ResponseEntity.status(ACCEPTED).build();
    }

    @PostMapping("/{elevatorId}/floor/{floor}")
    public ResponseEntity<Void> requestFloor(@PathVariable("elevatorId") int elevatorId, @PathVariable("floor") int floor) {
        log.info("Elevator {} requested on {} floor", elevatorId, floor);
        elevatorService.createRequestInsideElevator(elevatorId, floor);
        return ResponseEntity.status(ACCEPTED).build();
    }


}
