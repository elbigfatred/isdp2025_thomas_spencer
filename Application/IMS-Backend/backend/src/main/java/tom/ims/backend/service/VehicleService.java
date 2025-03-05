package tom.ims.backend.service;

import org.springframework.stereotype.Service;
import tom.ims.backend.model.Vehicle;
import tom.ims.backend.repository.VehicleRepository;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public List<Vehicle> getAvailableVehicles() {
        return vehicleRepository.findAllActiveVehiclesExceptCourier();
    }

    public Vehicle getBestVehicleForWeight(BigDecimal weight) {
        return vehicleRepository.findAll().stream()
                .filter(vehicle -> vehicle.getMaxWeight().compareTo(weight) >= 0)
                .filter(vehicle -> !"Courier".equalsIgnoreCase(vehicle.getVehicleType()))
                .min(Comparator.comparing(Vehicle::getMaxWeight))
                .orElse(null); // Returns smallest suitable vehicle or null
    }
}