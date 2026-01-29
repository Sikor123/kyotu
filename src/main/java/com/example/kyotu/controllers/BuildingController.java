package com.example.kyotu.controllers;

import com.example.kyotu.service.BuildingService;
import com.example.kyotu.service.model.BuildingSpec;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("api/v1/building")
public class BuildingController {

    private final BuildingService buildingService;

    @GetMapping("spec")
    public BuildingSpec getBuildingSpec(){
        log.info("Getting building spec.");
        return buildingService.getBuildingSpec();
    }
}
