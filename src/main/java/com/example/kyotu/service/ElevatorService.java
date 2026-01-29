package com.example.kyotu.service;

import com.example.kyotu.model.ElevatorState;
import com.example.kyotu.model.Elevator;
import com.example.kyotu.model.ElevatorUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.*;

@Slf4j
@Service
public class ElevatorService {

    private static final long UPDATES_PER_SECOND = (long) 1.0;
    private static final long UPDATE_PERIOD_MS = (long)(1000.0 / UPDATES_PER_SECOND);

    private final ConcurrentHashMap<Integer, Elevator> elevatorConcurrentHashMap;
    private final BlockingQueue<Integer> elevatorCallsQueue = new ArrayBlockingQueue<>(1024);
    private final SimpMessagingTemplate messagingTemplate;

    public ElevatorService(ScheduledExecutorService scheduler,
                           ConcurrentHashMap<Integer, Elevator> elevatorConcurrentHashMap,
                           SimpMessagingTemplate messagingTemplate) {
        this.elevatorConcurrentHashMap = elevatorConcurrentHashMap;
        this.messagingTemplate = messagingTemplate;
        scheduler.scheduleAtFixedRate(
                this::updateElevators,
                0,
                UPDATE_PERIOD_MS,
                TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(
                this::consumeElevatorFloorCall,
                0,
                UPDATE_PERIOD_MS/100,
                TimeUnit.MILLISECONDS);
    }

    public void createRequestOutsideElevator(int floorNumber){
        log.info("Elevator is called on floor {}.", floorNumber);
        if(noneElevatorGoingToThisFloor(floorNumber)){
            elevatorCallsQueue.add(floorNumber);
        }
    }

    private boolean noneElevatorGoingToThisFloor(int floorNumber) {
        return elevatorConcurrentHashMap.values().stream()
                .noneMatch(elevator -> elevator.getDesiredPosition() == (long) floorNumber);
    }

    public void createRequestInsideElevator(int elevatorId, int desiredFloor){
        log.info("Elevator {} is going To Floor {}.", elevatorId, desiredFloor);
        addElevatorFloorRequest(elevatorId, desiredFloor);
    }

    private void elevatorPositionUpdate(Elevator elevator){
        ElevatorUpdateDto elevatorUpdateDto = new ElevatorUpdateDto(elevator.getId(), elevator.getCurrentPosition(),elevator.getState());
        messagingTemplate.convertAndSend(
                "/topic/elevators/" + elevator.getId(),
                elevatorUpdateDto);

    }

    private void addElevatorFloorRequest(int elevatorId, int desiredFloor) {
        elevatorConcurrentHashMap.get(elevatorId).getElevatorOrdersQueue().add(desiredFloor);
    }

    private void updateElevators() {
    elevatorConcurrentHashMap.forEach(this::updateElevator);
    }

    private void updateElevator(Integer id, Elevator elevator) {
        if(isElevatorIdle(elevator)){
            elevator.setState(ElevatorState.IDLE);
        }
        if (elevator.getState().equals(ElevatorState.IDLE) && !elevator.getElevatorOrdersQueue().isEmpty()){
            try {
                elevator.setDesiredPosition(elevator.getElevatorOrdersQueue().take());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (isElevatorAtDesiredFloor(elevator)){
            elevator.setState(ElevatorState.WAITING);
            elevator.setWaitingTime(4);
        }
        if (elevator.getWaitingTime() > 0){
            elevator.setWaitingTime(elevator.getWaitingTime() - UPDATE_PERIOD_MS/1000);
            elevator.setState(ElevatorState.WAITING);
            elevator.setDoorsOpen(true);
        }
        elevator.setState(getElevatorDirection(elevator));
        moveElevator(elevator);
        elevatorPositionUpdate(elevator);
        log.info(elevator.toString());
    }

    private boolean isElevatorIdle(Elevator elevator) {
        return elevator.getState() == ElevatorState.WAITING && elevator.getWaitingTime() == 0;
    }

    private boolean isElevatorAtDesiredFloor(Elevator elevator) {
        return (elevator.getState() == ElevatorState.UP || elevator.getState() == ElevatorState.DOWN) && elevator.getCurrentPosition() == elevator.getDesiredPosition();
    }

    private void moveElevator(Elevator elevator) {
        if (elevator.getState() == ElevatorState.UP){
            elevator.setCurrentPosition(elevator.getCurrentPosition() + UPDATE_PERIOD_MS/1000);
        } else if(elevator.getState() == ElevatorState.DOWN) {
            elevator.setCurrentPosition(elevator.getCurrentPosition() - UPDATE_PERIOD_MS/1000);
        }
    }

    private ElevatorState getElevatorDirection(Elevator elevator) {
        if (elevator.getCurrentPosition() == elevator.getDesiredPosition()){
            return ElevatorState.IDLE;
        }
        if(elevator.getCurrentPosition() < elevator.getDesiredPosition()){
            return ElevatorState.UP;
        } else {
            return ElevatorState.DOWN;
        }
    }

    private synchronized void consumeElevatorFloorCall(){
        if(elevatorCallsQueue.size() > 0){
            boolean anyIdleElevators = elevatorConcurrentHashMap
                    .values()
                    .stream()
                    .anyMatch(elevator -> elevator.getState().equals(ElevatorState.IDLE));
            Integer floorNumberCall;
            try {
                floorNumberCall = elevatorCallsQueue.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            findClosestElevator(floorNumberCall).ifPresent(elevator -> elevator.getElevatorOrdersQueue().add(floorNumberCall));
        }

    }

    private Optional<Elevator> findClosestElevator(Integer floorNumberCall) {
        return elevatorConcurrentHashMap
                .values()
                .stream()
                .filter(elevator -> elevator.getState().equals(ElevatorState.IDLE))
                .min(Comparator.comparing(elevator ->
                        Math.abs(elevator.getCurrentPosition() - floorNumberCall)
                ));
    }
}
