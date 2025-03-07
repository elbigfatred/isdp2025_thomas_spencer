package tom.ims.backend.service;

import org.springframework.stereotype.Service;
import tom.ims.backend.model.Delivery;
import tom.ims.backend.repository.DeliveryRepository;

import java.util.List;
import java.util.Optional;

@Service
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;

    public DeliveryService(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    // ✅ Fetch all deliveries
    public List<Delivery> getAllDeliveries() {
        return deliveryRepository.findAll();
    }

    // ✅ Fetch delivery by ID
    public Delivery getDeliveryById(Integer id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found with ID: " + id));
    }
}