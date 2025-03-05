package tom.ims.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tom.ims.backend.model.Delivery;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {
}