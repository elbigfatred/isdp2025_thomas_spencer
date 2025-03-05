package tom.ims.backend.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tom.ims.backend.model.Vehicle;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    @Query("SELECT v FROM Vehicle v WHERE v.active = 1 AND v.vehicleType <> 'Courier'")
    List<Vehicle> findAllActiveVehiclesExceptCourier();
}