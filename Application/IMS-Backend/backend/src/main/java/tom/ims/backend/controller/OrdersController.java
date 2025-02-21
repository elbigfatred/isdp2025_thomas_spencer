package tom.ims.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tom.ims.backend.model.*;
import tom.ims.backend.repository.TxnRepository;
import tom.ims.backend.service.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    @Autowired private OrderService orderService;
    @Autowired private InventoryService inventoryService;
    @Autowired private TxnRepository txnRepository;
    @Autowired private TxnTypeService txnTypeService;
    @Autowired private TxnStatusService statusService;
    @Autowired
    private TxnStatusService txnStatusService;

    // ======================================================
    // 1️⃣ GENERAL ORDER RETRIEVAL & MANAGEMENT
    // ======================================================

    // Get all store and emergency orders (for filtering on frontend)
    @GetMapping("/all")
    public ResponseEntity<List<Txn>> getAllStoreAndEmergencyOrders() {
        try {
            List<Txn> orders = txnRepository.findAll();
            if (orders.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    //  Get all transactions (orders) for a specific site
    @GetMapping("/site/{siteId}")
    public ResponseEntity<List<Txn>> getOrdersBySite(@PathVariable Integer siteId) {
        try {
            List<Txn> orders = orderService.getOrdersBySite(siteId);
            if (orders.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    //  Get all transaction items for a specific txnID (order)
    @GetMapping("/{txnId}/items")
    public ResponseEntity<List<Txnitem>> getTxnItemsByTxnId(@PathVariable Integer txnId) {
        try {
            List<Txnitem> txnItems = orderService.getTxnItemsByTxnId(txnId);
            if (txnItems.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(txnItems);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    // ======================================================
    // 2️⃣ ORDER CREATION (STORE, BACKORDER)
    // ======================================================

    //  Check if an active Store Order exists for this site
    @GetMapping("/check-active")
    public ResponseEntity<Boolean> checkActiveOrder(@RequestParam Integer siteId) {
        boolean hasActiveOrder = orderService.hasActiveStoreOrder(siteId);
        return ResponseEntity.ok(hasActiveOrder);
    }
    //  Create a new Store Order transaction
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest) {
        try {
            if (orderService.hasActiveStoreOrder(orderRequest.getSiteIDTo())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("An active order already exists for this site.");
            }

            Txn newOrder = orderService.createOrderTransaction(orderRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(newOrder);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating order: " + e.getMessage());
        }
    }

    @PostMapping("/backorder")
    public ResponseEntity<?> createOrUpdateBackorder(@RequestBody BackorderRequest backorderRequest) {
        try {
            Txn backorderTxn = orderService.createOrUpdateBackorderTransaction(backorderRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(backorderTxn);
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to create/update backorder: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating or updating backorder: " + e.getMessage());
        }
    }

    // ======================================================
    // 3️⃣ ORDER UPDATING & ITEM MANAGEMENT
    // ======================================================

    //  Update an existing order with new item quantities
    @PutMapping("/{txnId}/update-items")
    public ResponseEntity<?> updateOrderItems(@PathVariable Integer txnId, @RequestBody OrderUpdateRequest updateRequest, @RequestParam String empUsername) {
        try {
            System.out.println("[DEBUG] Updating order ID: " + txnId);

            // ✅ Validate transaction ID in request
            if (!txnId.equals(updateRequest.getTxnID())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Transaction ID mismatch in request");
            }

            // ✅ Call service method to update items
            System.out.println("[DEBUG] Updating order ID: " + txnId);
            orderService.updateOrderItems(updateRequest, empUsername);

            // ✅ Fetch updated transaction and return it
            Txn updatedTxn = orderService.getOrderById(txnId);
            return ResponseEntity.ok(updatedTxn);

        } catch (Exception e) {
            System.out.println("[ERROR] Failed to update order: " + e.getMessage());
            e.printStackTrace(); // Print full error stack for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating order: " + e.getMessage());
        }
    }

    @PutMapping("/{txnId}/update-status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Integer txnId, @RequestParam String status, @RequestParam String empUsername) {
        try {
            System.out.println("[DEBUG] Updating order ID " + txnId + " to status: " + status + " by " + empUsername);
            Txn updatedTxn = orderService.updateOrderStatus(txnId, status, empUsername);
            return ResponseEntity.ok(updatedTxn);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating order status: " + e.getMessage());
        }
    }

    // ======================================================
    // 4️⃣ ORDER FULFILLMENT & PROCESSING
    // ======================================================

    // ✅ Submit an order (change status to SUBMITTED)
    @PutMapping("/{txnId}/submit")
    public ResponseEntity<?> submitOrder(@PathVariable Integer txnId) {
        try {
            System.out.println("[DEBUG] Submitting order ID: " + txnId);
            Txn submittedTxn = orderService.submitOrder(txnId);
            return ResponseEntity.ok(submittedTxn);
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to submit order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error submitting order: " + e.getMessage());
        }
    }

    // ✅ Submit an Emergency Order (change status to SUBMITTED)
    @PutMapping("/emergency/{txnId}/submit")
    public ResponseEntity<?> submitEmergencyOrder(@PathVariable Integer txnId) {
        try {
            System.out.println("[DEBUG] Submitting emergency order ID: " + txnId);
            Txn submittedTxn = orderService.submitEmergencyOrder(txnId); // ✅ Using the same method as store orders
            return ResponseEntity.ok(submittedTxn);
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to submit emergency order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error submitting emergency order: " + e.getMessage());
        }
    }

    //  Automatically submit all outstanding "NEW" orders every Tuesday at 11:59 PM
    @Scheduled(cron = "59 59 23 ? * TUE") // Runs every Tuesday at 11:59 PM
    @Transactional
    public void autoSubmitOrders() {
        try {
            System.out.println("[AUTO-SUBMIT] Checking for outstanding NEW orders...");

            // ✅ Fetch all NEW orders
            List<Txn> newOrders = orderService.getOrdersByStatus("NEW");

            if (newOrders.isEmpty()) {
                System.out.println("[AUTO-SUBMIT] No outstanding NEW orders found.");
                return;
            }

            // ✅ Submit each order (Store Orders only)
            for (Txn order : newOrders) {
                if(Objects.equals(order.getTxnType().getTxnType(), "Store Order")) {
                    orderService.autoSubmitOrder(order.getId());
                    System.out.println("[AUTO-SUBMIT] Submitted Order ID: " + order.getId());
                }
            }

            System.out.println("[AUTO-SUBMIT] Successfully submitted " + newOrders.size() + " orders.");

        } catch (Exception e) {
            System.out.println("[ERROR] Auto-submitting orders failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ======================================================
    // 5️⃣ EMERGENCY ORDER-SPECIFIC ENDPOINTS
    // ======================================================

    // ✅ Create a new Emergency Order transaction
    @PostMapping("/emergency")
    public ResponseEntity<?> createEmergencyOrder(@RequestBody OrderRequest orderRequest) {
        try {
            if (orderService.hasActiveEmergencyOrder(orderRequest.getSiteIDTo())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("An active emergency order already exists for this site.");
            }

            Txn newEmergencyOrder = orderService.createEmergencyOrderTransaction(orderRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(newEmergencyOrder);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating emergency order: " + e.getMessage());
        }
    }

    // ✅ Check if an active Emergency Order exists for this site
    @GetMapping("/check-active-emergency")
    public ResponseEntity<Boolean> checkActiveEmergencyOrder(@RequestParam Integer siteId) {
        boolean hasActiveEmergencyOrder = orderService.hasActiveEmergencyOrder(siteId);
        return ResponseEntity.ok(hasActiveEmergencyOrder);
    }
    // ✅ Get items below reorder threshold (Prepopulate Order)

    // ✅ Update an existing Emergency Order with new item quantities
    @PutMapping("/emergency/{txnId}/update-items")
    public ResponseEntity<?> updateEmergencyOrderItems(@PathVariable Integer txnId, @RequestBody OrderUpdateRequest updateRequest, @RequestParam String empUsername) {
        try {
            System.out.println("[DEBUG] Updating emergency order ID: " + txnId);

            // ✅ Validate transaction ID in request
            if (!txnId.equals(updateRequest.getTxnID())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Transaction ID mismatch in request");
            }

            // ✅ Call service method to update items
            orderService.updateOrderItems(updateRequest, empUsername);

            // ✅ Fetch updated transaction and return it
            Txn updatedTxn = orderService.getOrderById(txnId);
            return ResponseEntity.ok(updatedTxn);

        } catch (Exception e) {
            System.out.println("[ERROR] Failed to update emergency order: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating emergency order: " + e.getMessage());
        }
    }

    // ======================================================
    // 6️⃣ BACKORDER-SPECIFIC ENDPOINTS
    // ======================================================

    @PutMapping("/{txnId}/update-shipdate")
    public ResponseEntity<?> updateTxnShipDate(
            @PathVariable Integer txnId,
            @RequestParam String shipDate,
            @RequestParam String empUsername) {

        try {
            System.out.println("[DEBUG] Updating ship date for transaction ID: " + txnId + " to " + shipDate);

            // ✅ Call service method to update ship date
            orderService.updateTxnShipDate(txnId, shipDate, empUsername);

            // ✅ Fetch updated transaction
            Txn updatedTxn = orderService.getOrderById(txnId);
            return ResponseEntity.ok(updatedTxn);

        } catch (Exception e) {
            System.out.println("[ERROR] Failed to update ship date: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating ship date: " + e.getMessage());
        }
    }


    // ======================================================
    // 7️⃣ UTILITY & DEBUGGING METHODS (OPTIONAL)
    // ======================================================

    @GetMapping("/prepopulate")
    public ResponseEntity<List<Inventory>> prepopulateOrder(@RequestParam Integer siteId) {
        List<Inventory> reorderItems = inventoryService.getItemsBelowThreshold(siteId);

        System.out.println("[DEBUG] Found " + reorderItems.size() + " items below reorder threshold for site " + siteId);

        // Adjust quantity to respect case size
        for (Inventory inv : reorderItems) {
            Item item = inv.getItem(); // Get the item details
            int caseSize = item.getCaseSize(); // Get the case size
            int currentQty = inv.getQuantity();
            int reorderThreshold = inv.getReorderThreshold();
            int optimumThreshold = inv.getOptimumThreshold();
            int neededQty = optimumThreshold - currentQty; // Raw needed quantity

            System.out.println("[DEBUG] Processing item: " + item.getName() + " (Item ID: " + item.getId() + ")");
            System.out.println("  - Current Stock: " + currentQty);
            System.out.println("  - Reorder Threshold: " + reorderThreshold);
            System.out.println("  - Optimum Threshold: " + optimumThreshold);
            System.out.println("  - Case Size: " + caseSize);
            System.out.println("  - Needed Quantity (before case adjustment): " + neededQty);

            if (caseSize > 0) { // Avoid division by zero
                int adjustedQty = (int) Math.ceil((double) neededQty / caseSize) * caseSize;

                System.out.println("  - Adjusted Quantity (rounded to case size): " + adjustedQty);

                inv.setQuantity(adjustedQty); // Ensure we round up to the nearest full case
            } else {
                System.out.println("[WARNING] Item " + item.getId() + " has an invalid case size (" + caseSize + ")");
            }
        }

        return ResponseEntity.ok(reorderItems);
    }

    @GetMapping("/allorderstatuses")
    public ResponseEntity<List<Txnstatus>> getAllOrderStatues() {
        List<Txnstatus> alltypes = new ArrayList<>();
        try {
            alltypes = txnStatusService.findAll();
            System.out.println("[DEBUG] Found " + alltypes.size() + " transaction types");
            return ResponseEntity.ok(alltypes);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(alltypes);
        }
    }
}