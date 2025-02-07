package tom.ims.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tom.ims.backend.model.*;
import tom.ims.backend.service.InventoryService;
import tom.ims.backend.service.OrderService;
import tom.ims.backend.service.ItemService;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ItemService itemService;

    // ✅ Check if an active Store Order exists for this site
    @GetMapping("/check-active")
    public ResponseEntity<Boolean> checkActiveOrder(@RequestParam Integer siteId) {
        boolean hasActiveOrder = orderService.hasActiveStoreOrder(siteId);
        return ResponseEntity.ok(hasActiveOrder);
    }

//    // ✅ Get items below reorder threshold (Prepopulate Order)
//    @GetMapping("/prepopulate")
//    public ResponseEntity<List<Inventory>> prepopulateOrder(@RequestParam Integer siteId) {
//        List<Inventory> reorderItems = inventoryService.getItemsBelowThreshold(siteId);
//        return ResponseEntity.ok(reorderItems);
//    }

    // ✅ Get items below reorder threshold (Prepopulate Order)
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

    // ✅ Create a new Store Order transaction
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

    // ✅ Get all transactions (orders) for a specific site
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

    // ✅ Get all transaction items for a specific txnID (order)
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

    // ✅ Update an existing order with new item quantities
    @PutMapping("/{txnId}/update-items")
    public ResponseEntity<?> updateOrderItems(@PathVariable Integer txnId, @RequestBody OrderUpdateRequest updateRequest) {
        try {
            System.out.println("[DEBUG] Updating order ID: " + txnId);

            // ✅ Validate transaction ID in request
            if (!txnId.equals(updateRequest.getTxnID())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Transaction ID mismatch in request");
            }

            // ✅ Call service method to update items
            orderService.updateOrderItems(updateRequest);

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
}