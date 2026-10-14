package com.dispatch.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dispatch.dto.VehicleRequest;
import com.dispatch.entity.Vehicle;
import com.dispatch.repository.VehicleRepository;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public void saveVehicles(List<VehicleRequest> requests) {
        // Guard clause to handle empty or invalid payloads gracefully
        if (requests == null || requests.isEmpty()) {
            return;
        }

        List<Vehicle> vehicles = requests.stream()
                .map(request -> new Vehicle(
                        request.vehicleId(),
                        request.capacity(),
                        request.currentLatitude(),
                        request.currentLongitude(),
                        request.currentAddress()))
                .toList();

        vehicleRepository.saveAll(vehicles);
    }
}
