package tom.ims.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tom.ims.backend.model.*;
import tom.ims.backend.repository.*;
import tom.ims.backend.service.*;

import java.util.List;

@RestController
@RequestMapping("/api/supplierorders")
public class SupplierOrderController {

    @Autowired
    private OrderService orderService;
    @Autowired private InventoryService inventoryService;
    @Autowired private TxnRepository txnRepository;
    @Autowired private TxnTypeService txnTypeService;
    @Autowired private TxnStatusService statusService;
    @Autowired private TxnStatusService txnStatusService;
    @Autowired private TxnTypeRepository txnTypeRepository;
    @Autowired private SiteRepository siteRepository;
    @Autowired private TxnStatusRepository txnStatusRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private TxnItemsRepository txnItemsRepository;
    @Autowired private TxnauditService txnauditService;
    @Autowired private ItemService itemService;
    @Autowired private SiteService siteService;
    private final int warehouseId = 2;

    @PutMapping("/{txnId}/update-items")
    public ResponseEntity<?> updateOrderItems(@PathVariable Integer txnId, @RequestBody OrderUpdateRequest updateRequest, @RequestParam String empUsername) {
        try {
            System.out.println("[DEBUG] Updating supplier order ID: " + txnId);

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

    @GetMapping("/prepopulate")
    public ResponseEntity<List<Inventory>> prepopulateOrder() {
        List<Inventory> reorderItems = inventoryService.getSupplierItemsBelowThreshold();

        System.out.println("[DEBUG] Found " + reorderItems.size() + " items below reorder threshold for site (with active suppliers) " + warehouseId);

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

    @PostMapping("/createNew")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest) {
        try {
            Txn activeOrder = orderService.getActiveSupplierOrder();
            if (activeOrder != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("An active supplier order already exists.");
            }

            Txn newOrder = orderService.createSupplierOrder(orderRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(newOrder);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating order: " + e.getMessage());
        }
    }

    @GetMapping("/check-active")
    public ResponseEntity<Integer> checkActiveOrder() {
        Txn activeOrder = orderService.getActiveSupplierOrder();
        if (activeOrder != null) {
            return ResponseEntity.ok(activeOrder.getId()); // return txnID
        } else {
            return ResponseEntity.ok(null); // no active order
        }
    }

    @PutMapping("/{txnId}/submit")
    public ResponseEntity<?> submitSupplierOrder(@PathVariable Integer txnId, @RequestParam String empUsername) {
        try {
            Txn updatedTxn = orderService.submitSupplierOrder(txnId, empUsername);
            return ResponseEntity.ok(updatedTxn);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating order status: " + e.getMessage());
        }
    }
}
