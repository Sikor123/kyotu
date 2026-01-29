package com.example.kyotu.service;

import com.example.kyotu.config.PropertiesConfig;
import com.example.kyotu.service.model.BuildingSpec;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BuildingService {

    private final PropertiesConfig propertiesConfig;

    public BuildingSpec getBuildingSpec(){
        return new BuildingSpec(propertiesConfig.getNumberOfFloors(), propertiesConfig.getNumberOfElevators());
    }
}
