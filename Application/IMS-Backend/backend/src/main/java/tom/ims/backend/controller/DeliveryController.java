package tom.ims.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tom.ims.backend.model.Delivery;
import tom.ims.backend.model.DeliveryTxnDTO;
import tom.ims.backend.model.Txn;
import tom.ims.backend.model.Vehicle;
import tom.ims.backend.repository.DeliveryRepository;
import tom.ims.backend.repository.TxnRepository;
import tom.ims.backend.service.OrderService;
import tom.ims.backend.service.VehicleService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    @Autowired private OrderService orderService;
    @Autowired private VehicleService vehicleService;
    @Autowired private TxnRepository txnRepository;
    @Autowired private DeliveryRepository deliveryRepository;

    @GetMapping("/upcoming")
    public ResponseEntity<List<DeliveryTxnDTO>> getUpcomingDeliveries() {
        List<DeliveryTxnDTO> upcomingTxns = orderService.findUpcomingDeliveries();
        return ResponseEntity.ok(upcomingTxns);
    }

    @GetMapping("/availableVehicles")
    public List<Vehicle> getAvailableVehicles() {
        return vehicleService.getAvailableVehicles();
    }

    @PostMapping("/assignDelivery")
    public ResponseEntity<?> assignDelivery(@RequestBody List<Integer> txnIds) {
        if (txnIds.isEmpty()) {
            System.out.println("🚨 No transactions provided for delivery assignment.");
            return ResponseEntity.badRequest().body("No transactions provided.");
        }

        // Fetch all transactions by IDs
        List<Txn> transactions = txnRepository.findAllById(txnIds);

        if (transactions.isEmpty()) {
            System.out.println("🚨 No valid transactions found for given IDs: " + txnIds);
            return ResponseEntity.badRequest().body("No valid transactions found.");
        }

        // Ensure all transactions are in 'ASSEMBLED' status
        boolean allAssembled = transactions.stream()
                .allMatch(txn -> txn.getTxnStatus().getStatusName().equals("ASSEMBLED"));

        if (!allAssembled) {
            System.out.println("🚨 Some transactions are NOT in ASSEMBLED status.");
            return ResponseEntity.badRequest().body("All transactions must be in ASSEMBLED status.");
        }

        // Ensure none of the transactions already have a delivery ID
        boolean anyAlreadyAssigned = transactions.stream()
                .anyMatch(txn -> txn.getDeliveryID() != null);

        if (anyAlreadyAssigned) {
            System.out.println("🚨 Some transactions already have a delivery ID assigned.");
            return ResponseEntity.badRequest().body("Some transactions already have a delivery ID assigned.");
        }

        // Convert LocalDateTime to Instant (Assume UTC timezone)
        Instant deliveryDate = transactions.get(0).getShipDate().atZone(ZoneId.of("UTC")).toInstant();
        System.out.println("📅 Delivery Date Determined: " + deliveryDate);

        // Calculate total weight
        BigDecimal totalWeight = transactions.stream()
                .map(orderService::calculateTotalWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        System.out.println("⚖️ Total weight calculated: " + totalWeight + " kg");

        // Determine the best vehicle
        Vehicle bestVehicle = vehicleService.getBestVehicleForWeight(totalWeight);

        if (bestVehicle == null) {
            System.out.println("🚨 No suitable vehicle found for total weight: " + totalWeight + " kg");
            return ResponseEntity.badRequest().body("No suitable vehicle found for the given weight.");
        }

        System.out.println("🚚 Best vehicle selected: " + bestVehicle.getVehicleType() +
                " (Max weight: " + bestVehicle.getMaxWeight() + " kg)");

        // Calculate distance cost
        BigDecimal distanceCost = transactions.stream()
                .map(txn -> BigDecimal.valueOf(txn.getSiteIDTo().getDistanceFromWH())
                        .multiply(bestVehicle.getCostPerKm()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        System.out.println("💰 Total distance cost calculated: $" + distanceCost);

        // Create new delivery record
        Delivery delivery = new Delivery();
        delivery.setDeliveryDate(deliveryDate);
        delivery.setVehicle(bestVehicle);
        delivery.setDistanceCost(distanceCost);
        delivery.setNotes("Automated delivery assignment");

        delivery = deliveryRepository.save(delivery); // Save the new delivery record
        System.out.println("✅ New delivery created with ID: " + delivery.getId());

        // Assign the new deliveryID to each transaction
        Delivery finalDelivery = delivery;
        transactions.forEach(txn -> txn.setDeliveryID(finalDelivery));
        txnRepository.saveAll(transactions);

        System.out.println("✅ Successfully assigned delivery ID " + finalDelivery.getId() +
                " to " + transactions.size() + " transactions.");

        return ResponseEntity.ok(transactions);
    }



}