package tom.ims.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tom.ims.backend.model.*;
import tom.ims.backend.repository.*;
import tom.ims.backend.service.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    @Autowired private OrderService orderService;
    @Autowired private VehicleService vehicleService;
    @Autowired private TxnRepository txnRepository;
    @Autowired private DeliveryRepository deliveryRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private TxnauditService txnauditService;
    @Autowired private TxnItemsRepository txnItemsRepository;
    @Autowired private EmployeeService employeeService;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private TxnauditService auditService;
    @Autowired private TxnStatusService txnStatusService;
    @Autowired private DeliveryService deliveryService; // Inject DeliveryService


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
    public ResponseEntity<?> assignDelivery(@RequestBody List<Integer> txnIds, @RequestParam String empUsername) {
        if (txnIds.isEmpty()) {
            System.out.println("No transactions provided for delivery assignment.");
            return ResponseEntity.badRequest().body("No transactions provided.");
        }

        // Fetch all transactions by IDs
        List<Txn> transactions = txnRepository.findAllById(txnIds);

        if (transactions.isEmpty()) {
            System.out.println("No valid transactions found for given IDs: " + txnIds);
            return ResponseEntity.badRequest().body("No valid transactions found.");
        }

        // Ensure all transactions are in 'ASSEMBLED' or ASSEMBLING or RECEIVED status
        boolean allAssembled = transactions.stream()
                .allMatch(txn -> {
                    String status = txn.getTxnStatus().getStatusName();
                    return status.equals("ASSEMBLED") || status.equals("ASSEMBLING") || status.equals("RECEIVED");
                });

        if (!allAssembled) {
            System.out.println("Some transactions are NOT in ASSEMBLED status.");
            return ResponseEntity.badRequest().body("All transactions must be in ASSEMBLED status.");
        }

        // Ensure none of the transactions already have a delivery ID
        boolean anyAlreadyAssigned = transactions.stream()
                .anyMatch(txn -> txn.getDeliveryID() != null);

        if (anyAlreadyAssigned) {
            System.out.println("Some transactions already have a delivery ID assigned.");
            return ResponseEntity.badRequest().body("Some transactions already have a delivery ID assigned.");
        }

        // Fetch the Employee assigning the delivery
        Employee assigningEmployee = employeeRepository.findByUsername(empUsername)
                .orElse(null);

        if (assigningEmployee == null) {
            System.out.println("🚨 Employee not found: " + empUsername);
            return ResponseEntity.badRequest().body("Employee not found.");
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
        //add 2 hours of time cost
        distanceCost = distanceCost.add(bestVehicle.getHourlyTruckCost().multiply(BigDecimal.valueOf(2)));

        System.out.println("💰 Total distance cost calculated: $" + distanceCost);

        // Create new delivery record
        Delivery delivery = new Delivery();
        delivery.setDeliveryDate(deliveryDate);
        delivery.setVehicle(bestVehicle);
        delivery.setDistanceCost(distanceCost);
        delivery.setNotes("Automated delivery assignment");

        delivery = deliveryRepository.save(delivery); // Save the new delivery record
        System.out.println("✅ New delivery created with ID: " + delivery.getId());

        // Assign the new deliveryID to each transaction and log the audit entry
        Delivery finalDelivery = delivery;
        transactions.forEach(txn -> {
            txn.setDeliveryID(finalDelivery);
            txnRepository.save(txn); // Ensure each transaction is saved

            // ✅ Create an audit entry
            String actionDetails = "Order ID " + txn.getId() + " assigned Delivery ID " + finalDelivery.getId() + " by " + empUsername;
            txnauditService.createAuditEntry(txn, assigningEmployee, actionDetails);

            System.out.println("📝 Audit logged for Txn ID: " + txn.getId());
        });        txnRepository.saveAll(transactions);

        System.out.println("✅ Successfully assigned delivery ID " + finalDelivery.getId() +
                " to " + transactions.size() + " transactions.");

        return ResponseEntity.ok(transactions);
    }


    @GetMapping("/shipping-today")
    public ResponseEntity<List<Txn>> getOrdersForShippingReceiving() {
        LocalDate today = LocalDate.now();

        List<Txn> orders = txnRepository.findByTxnTypeAndDeliveryAssignedToday(today);

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Delivery>> getAllDeliveries() {
        List<Delivery> deliveries = deliveryService.getAllDeliveries();
        return ResponseEntity.ok(deliveries);
    }

}